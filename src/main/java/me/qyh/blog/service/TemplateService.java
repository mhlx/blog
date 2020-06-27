package me.qyh.blog.service;

import me.qyh.blog.BlogContext;
import me.qyh.blog.Message;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.entity.Template;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CommentMapper;
import me.qyh.blog.mapper.TemplateMapper;
import me.qyh.blog.utils.Compile;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StreamUtils;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.TemplateQueryParam;
import me.qyh.blog.web.template.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 模板管理器，用来注册页面
 *
 * @author wwwqyhme
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TemplateService
        implements HandlerMapping, CommentModuleHandler<Template> {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private final StampedLock lock = new StampedLock();
    private final PathMatcher pathMatcher = new AntPathMatcher();
    public static final String ROOT_TEMPLATE_KEY = TemplateService.class.getName() + ".root_template.request";
    public static final String TEMPLATE_NAME = "template";

    private final Path regPath = Paths.get(System.getProperty("user.home")).resolve("blog/defaultTemplatesReg");

    private final Map<String, Template> urlPatternMap = new HashMap<>();
    private final Map<String, Template> previewUrlPatternMap = new HashMap<>();

    private final Map<String, Template> fragmentMap = new HashMap<>();
    private final Map<String, Template> previewFragmentMap = new HashMap<>();

    private String previewIp;// current preview ip

    private final TemplateMapper templateMapper;
    private final TransactionTemplate readOnlyTemplate;
    private final TransactionTemplate writeTemplate;

    private static final String ALL_ERROR_TEMPLATE_NAME = "error/*";
    private static final String COMMENT_MODULE_NAME = "template";
    private static final String ERROR_PATH = "/error";

    private List<SystemTemplate> defaultTemplates;

    private int previewId;

    private final CommentMapper commentMapper;

    private static final String DEFAULT_SOURCE_CODE = "public class TemplateHelper {}";
    private Object templateHelper;
    private final Path templateHelperSourceCodePath = Paths.get(System.getProperty("user.home")).resolve("blog/templateHelper.java");

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    public TemplateService(TemplateMapper templateMapper, PlatformTransactionManager transactionManager,
                           CommentMapper commentMapper) {
        this.templateMapper = templateMapper;
        this.commentMapper = commentMapper;
        this.readOnlyTemplate = new TransactionTemplate(transactionManager);
        this.readOnlyTemplate.setReadOnly(true);
        this.writeTemplate = new TransactionTemplate(transactionManager);
        this.writeTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        registerAllDefaultTemplates();
        this.getDefaultTemplates().stream().filter(t -> t.resetAfterRemove && t.getName() != null).forEach(t -> fragmentMap.put(t.getName(), t));
        registerAllEnabledTemplates();
        setTemplateHelper();
    }

    /**
     * 分页查询模板
     *
     * @param param 分页参数
     * @return 分页结果
     */
    public PageResult<Template> queryTemplate(TemplateQueryParam param) {
        return readOnlyTemplate.execute(status -> {
            int count = templateMapper.selectCount(param);
            if (count > 0) {
                List<Template> templates = templateMapper.selectPage(param);
                return new PageResult<>(param, count, templates);
            }
            return new PageResult<>(param, count, Collections.emptyList());
        });
    }

    /**
     * 注册一个模板
     *
     * @param template 模板
     */
    public Integer registerTemplate(Template template) {
        checkReservedPattern(template);
        long stamp = lock.writeLock();
        try {
            return writeTemplate.execute(status -> {
                disableEnable(template);
                template.setCreateTime(LocalDateTime.now());
                templateMapper.insert(template);
                if (template.getEnable()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            makeTemplateVisible(template);
                        }
                    });
                }
                return template.getId();
            });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 更新一个模板
     *
     * @param template 模板
     */
    public void updateTemplate(Template template) {
        checkReservedPattern(template);
        long stamp = lock.writeLock();
        try {
            writeTemplate.executeWithoutResult(status -> {
                // find old template
                Template old = templateMapper.selectById(template.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("template.notExists", "模板不存在"));
                disableEnable(template);
                template.setModifyTime(LocalDateTime.now());
                templateMapper.update(template);

                if (old.getEnable() || template.getEnable()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            if (old.getEnable()) {
                                if (old.getPattern() != null) {
                                    urlPatternMap.remove(old.getPattern());
                                } else {
                                    removeFragment(old.getName());
                                }
                            }
                            if (template.getEnable()) {
                                makeTemplateVisible(template);
                            }
                        }
                    });
                }
            });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 注册一个预览模板
     *
     * @param template 模板
     */
    public int registerPreviewTemplate(Template template) {
        checkReservedPattern(template);
        long stamp = lock.writeLock();
        try {
            previewId++;
            template.setId(previewId);
            if (template.getPattern() != null) {
                previewUrlPatternMap.put(template.getPattern(), template);
            } else {
                previewFragmentMap.put(template.getName(), template);
            }
            previewIp = BlogContext.getIP().orElse(null);
            return template.getId();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取一个模板，如果模板处于启用状态，并且当前IP是预览IP，那么模板内容会被替换为预览模板内容(如果存在对应名称或者路径的模板)
     *
     * @param id 模板id
     * @return 模板明细
     */
    public Optional<Template> getTemplate(int id) {
        return readOnlyTemplate.execute(status -> {
            Optional<Template> opTemplate = templateMapper.selectById(id);
            if (opTemplate.isPresent() && opTemplate.get().getEnable() && previewIp != null
                    && previewIp.equals(BlogContext.getIP().orElse(null))) {
                long stamp = lock.readLock();
                try {
                    Template template = opTemplate.get();
                    Optional.ofNullable(template.getName() != null ? previewFragmentMap.get(template.getName())
                            : previewUrlPatternMap.get(template.getPattern())).map(Template::getContent)
                            .ifPresent(template::setContent);
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return opTemplate;
        });
    }

    /**
     * 删除一个模板
     *
     * @param id 模板id
     */
    public void deleteTemplate(int id) {
        long stamp = lock.writeLock();
        try {
            writeTemplate.executeWithoutResult(status -> {
                Optional<Template> opOld = templateMapper.selectById(id);
                if (opOld.isPresent()) {
                    Template old = opOld.get();
                    templateMapper.deleteById(id);
                    commentMapper.deleteByModule(new CommentModule(COMMENT_MODULE_NAME, id));
                    if (old.getEnable()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {
                                if (old.getPattern() != null) {
                                    previewUrlPatternMap.remove(old.getPattern());
                                    urlPatternMap.remove(old.getPattern());
                                } else {
                                    previewFragmentMap.remove(old.getName());
                                    removeFragment(old.getName());
                                }
                            }
                        });
                    }
                }
            });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 移除所有的预览模板
     */
    public void clearPreviewTemplates() {
        long stamp = lock.writeLock();
        try {
            previewUrlPatternMap.clear();
            previewFragmentMap.clear();
            previewIp = null;
            previewId = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取所有的预览模板
     *
     * @return 预览模板
     */
    public List<Template> getPreviewTemplates() {
        long stamp = lock.readLock();
        try {
            List<Template> templates = new ArrayList<>();
            templates.addAll(previewUrlPatternMap.values());
            templates.addAll(previewFragmentMap.values());
            return templates;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * 删除某一个预览模板
     *
     * @param id 模板id
     */
    public void deletePreviewTemplate(int id) {
        long stamp = lock.writeLock();
        try {
            previewFragmentMap.entrySet().removeIf(e -> e.getValue().getId() == id);
            previewUrlPatternMap.entrySet().removeIf(e -> e.getValue().getId() == id);
            if (previewFragmentMap.isEmpty() && previewUrlPatternMap.isEmpty()) {
                previewIp = null;
                previewId = 0;
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取默认模板
     *
     * @return 默认模板
     */
    public List<SystemTemplate> getDefaultTemplates() {
        if (this.defaultTemplates == null) {
            synchronized (this) {
                if (this.defaultTemplates == null) {
                    String indexTemplate = readResourceToString(new ClassPathResource("defaultTemplates/index.html"));
                    String articleTemplate = readResourceToString(
                            new ClassPathResource("defaultTemplates/article.html"));
                    String momentsTemplate = readResourceToString(
                            new ClassPathResource("defaultTemplates/moments.html"));
                    String momentTemplate = readResourceToString(new ClassPathResource("defaultTemplates/moment.html"));
                    String navTemplate = readResourceToString(new ClassPathResource("defaultTemplates/nav.html"));
                    String errorContent = readResourceToString(new ClassPathResource("defaultTemplates/error.html"));
                    String errorPageErrorContent = readResourceToString(
                            new ClassPathResource("defaultTemplates/errorPageError.html"));
                    String unlockContent = readResourceToString(new ClassPathResource("defaultTemplates/unlock.html"));
                    String markdownRenderContent = readResourceToString(
                            new ClassPathResource("defaultTemplates/markdown_render.html"));

                    SystemTemplate index = new SystemTemplate(null, "/", indexTemplate, true, false);
                    SystemTemplate article = new SystemTemplate(null, "/articles/{idOrAlias}", articleTemplate, true,
                            false);
                    SystemTemplate moments = new SystemTemplate(null, "/moments", momentsTemplate, true, false);
                    SystemTemplate moment = new SystemTemplate(null, "/moments/{id}", momentTemplate, true, false);

                    SystemTemplate nav = new SystemTemplate("nav", null, navTemplate, true, false);
                    SystemTemplate error = new SystemTemplate(ALL_ERROR_TEMPLATE_NAME, null, errorContent, false, true);
                    SystemTemplate errorPageError = new SystemTemplate("errorPageError", null, errorPageErrorContent,
                            false, true);
                    SystemTemplate unlock = new SystemTemplate("unlock", null, unlockContent, false, true);
                    SystemTemplate markdownRender = new SystemTemplate("markdown_render", null, markdownRenderContent,
                            false, true);
                    List<SystemTemplate> defaultTemplates = List.of(index, article, moments, moment, nav, error,
                            errorPageError, unlock, markdownRender);
                    defaultTemplates.forEach(t -> {
                        t.setEnable(true);
                        t.setAllowComment(false);
                    });
                    this.defaultTemplates = defaultTemplates;
                }
            }
        }
        return List.copyOf(this.defaultTemplates);
    }

    /**
     * 将预览模板保存到模板中
     *
     * <p>
     * <b>如果对应路径|名称的模板不存在或者未启用，将会插入一个新的模板</b>
     * </p>
     */
    public void mergePreviewTemplates() {
        long stamp = lock.writeLock();
        try {
            if (previewFragmentMap.isEmpty() && previewUrlPatternMap.isEmpty()) {
                return;
            }

            writeTemplate.executeWithoutResult(status -> {

                List<Template> merges = new ArrayList<>();

                Stream.concat(previewFragmentMap.values().stream(), previewUrlPatternMap.values().stream())
                        .forEach(template -> {
                            String pattern = template.getPattern();
                            String name = template.getName();
                            Optional<Template> dbOptional;
                            if (pattern != null) {
                                dbOptional = templateMapper.selectEnabledByPattern(pattern);
                            } else {
                                dbOptional = templateMapper.selectEnabledByName(name);
                            }
                            if (dbOptional.isEmpty()) {
                                Template newTemplate = new Template();
                                newTemplate.setName(name);
                                newTemplate.setPattern(pattern);
                                newTemplate.setAllowComment(false);
                                newTemplate.setContent(template.getContent());
                                newTemplate.setCreateTime(LocalDateTime.now());
                                newTemplate.setEnable(true);
                                templateMapper.insert(newTemplate);
                                merges.add(newTemplate);
                            } else {
                                Template db = dbOptional.get();
                                db.setContent(template.getContent());
                                db.setModifyTime(LocalDateTime.now());
                                templateMapper.update(db);
                                merges.add(db);
                            }
                        });

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        for (Template template : merges) {
                            makeTemplateVisible(template);
                        }

                        previewFragmentMap.clear();
                        previewUrlPatternMap.clear();
                        previewIp = null;
                        previewId = 0;
                    }
                });
            });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public boolean isPreviewRequest(HttpServletRequest request) {
        return this.previewIp != null && this.previewIp.equals(BlogContext.getIP().orElse(null));
    }

    public Object getTemplateHelper() {
        return this.templateHelper;
    }

    public String getTemplateHelperSourceCode() {
        if (Files.exists(templateHelperSourceCodePath)) {
            String content = readResourceToString(new FileSystemResource(templateHelperSourceCodePath));
            if (content.isBlank()) {
                return DEFAULT_SOURCE_CODE;
            }
            return content;
        }
        return DEFAULT_SOURCE_CODE;
    }

    public synchronized void updateTemplateHelperSourceCode(String code) {
        FileUtils.createFile(templateHelperSourceCodePath);
        try {
            Files.writeString(templateHelperSourceCodePath, code);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            this.templateHelper = compile(code);
        } catch (Compile.CompileException ex) {
            throw new LogicException(new CompileErrorMessage("templateService.templateHelper.compile.error", "编译失败", ex.getErrors()));
        }
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {

        // only support get method
        if (!"get".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);

        if (lookupPath.equalsIgnoreCase(ERROR_PATH)) {
            return null;// use system error controller
        }

        request.setAttribute(LOOKUP_PATH, lookupPath);

        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.readLock()) {
                if (stamp == 0L)
                    continue;
                HandlerExecutionChain chain = findChain(lookupPath, request);
                if (!lock.validate(stamp))
                    continue;
                return chain;
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    private HandlerExecutionChain findChain(String lookupPath, HttpServletRequest request) {
        // first lookup best preview match
        Template bestPreview = null;
        boolean preview = isPreviewRequest(request);
        if (preview) {
            bestPreview = lookupTemplate(lookupPath, previewUrlPatternMap);
        }
        // then lookup best match
        Template best = lookupTemplate(lookupPath, urlPatternMap);

        Template finalTemplate = null;
        if (bestPreview != null && best != null) {
            // compare bestPreview & best
            Comparator<String> comparator = pathMatcher.getPatternComparator(lookupPath);
            int compare = comparator.compare(bestPreview.getPattern(), best.getPattern());
            if (compare == 0) {
                finalTemplate = bestPreview;
            } else if (compare < 0) {
                finalTemplate = best;
            }
        } else {
            finalTemplate = bestPreview == null ? best : bestPreview;
        }

        if (finalTemplate != null) {
            Map<String, String> uriVariables = pathMatcher.extractUriTemplateVariables(finalTemplate.getPattern(),
                    lookupPath);
            Map<String, String> decodedUriVariables = urlPathHelper.decodePathVariables(request, uriVariables);
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decodedUriVariables);
            request.setAttribute(ROOT_TEMPLATE_KEY, new Template(finalTemplate));
            return new HandlerExecutionChain(TEMPLATE_NAME);
        } else {
            // remove request attributes
            request.removeAttribute(ROOT_TEMPLATE_KEY);
            request.removeAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        }

        return null;
    }

    private Template lookupTemplate(String lookupPath, Map<String, Template> urlPatternMap) {
        if (urlPatternMap.isEmpty()) {
            return null;
        }
        // first lookup best match
        Template best = urlPatternMap.get(lookupPath);
        if (best == null) {
            List<String> patterns = new ArrayList<>();
            // iterator all template
            for (Map.Entry<String, Template> it : urlPatternMap.entrySet()) {
                String pattern = it.getKey();
                String match = getMatchingPattern(pattern, lookupPath);
                if (match != null) {
                    patterns.add(pattern);
                }
            }
            int size = patterns.size();
            Comparator<String> comparator = pathMatcher.getPatternComparator(lookupPath);
            if (size > 1) {
                patterns.sort(comparator);
            }

            if (size > 0) {
                String first = patterns.get(0);
                if (size > 1) {
                    String second = patterns.get(1);

                    if (comparator.compare(first, second) == 0) {
                        Template _first = urlPatternMap.get(first);
                        Template _second = urlPatternMap.get(second);
                        // need to compare template'id
                        if (_first.getId() > _second.getId()) {
                            best = _first;
                        } else {
                            best = _second;
                        }
                    } else {
                        best = urlPatternMap.get(first);
                    }
                } else {
                    best = urlPatternMap.get(first);
                }
            }
        }
        return best;
    }

    private String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        if (pathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }
        return null;
    }

    public Optional<Template> findTemplate(HttpServletRequest request, String templateName) {
        if (TEMPLATE_NAME.equals(templateName)) {
            return Optional.ofNullable((Template) request.getAttribute(ROOT_TEMPLATE_KEY));
        }
        boolean isPreviewRequest = isPreviewRequest(request);
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.readLock()) {
                if (stamp == 0L)
                    continue;
                Template lookupResult = findFragment(templateName, isPreviewRequest);
                if (!lock.validate(stamp))
                    continue;
                return Optional.ofNullable(lookupResult);
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    private Template findFragment(String templateName, boolean previewRequest) {
        Template template = null;
        if (previewRequest) {
            template = previewFragmentMap.get(templateName);
        }
        if (template == null) {
            template = fragmentMap.get(templateName);
        }
        if (template == null && StringUtils.startsWithIgnoreCase(templateName, "error/")
                && !templateName.toLowerCase().equals(ALL_ERROR_TEMPLATE_NAME)) {
            return findFragment(ALL_ERROR_TEMPLATE_NAME, previewRequest);
        }
        return template;
    }

    private void disableEnable(Template template) {
        String pattern = template.getPattern();
        // no need to check if disable
        if (!template.getEnable()) {
            return;
        }
        Template disable = null;
        if (pattern != null) {
            Optional<Template> opDb = templateMapper.selectEnabledByPattern(pattern);
            if (opDb.isPresent()) {
                disable = opDb.get();
            }
        } else {
            String alias = template.getName();
            // find Template by alias
            Optional<Template> opDb = templateMapper.selectEnabledByName(alias);
            if (opDb.isPresent()) {
                disable = opDb.get();
            }
        }
        if (disable != null) {
            disable.setEnable(false);
            templateMapper.update(disable);
        }
    }

    private void removeFragment(String alias) {
        fragmentMap.remove(alias);
        getDefaultTemplates().stream().filter(t -> t.resetAfterRemove && alias.equals(t.getName())).findAny().ifPresent(t -> fragmentMap.put(alias, t));
    }

    private void registerAllDefaultTemplates() {
        if (Files.exists(regPath)) {
            return;
        }
        List<SystemTemplate> defaultTemplates = getDefaultTemplates().stream().filter(t -> t.regWhenFirstStartup)
                .collect(Collectors.toList());
        writeTemplate.executeWithoutResult(status -> {
            for (Template template : defaultTemplates) {
                if ((template.getName() != null && templateMapper.selectEnabledByName(template.getName()).isEmpty())
                        || (template.getPattern() != null
                        && templateMapper.selectEnabledByPattern(template.getPattern()).isEmpty())) {
                    template.setCreateTime(LocalDateTime.now());
                    templateMapper.insert(template);
                }
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    FileUtils.createFile(regPath);
                }
            });
        });
    }

    private void registerAllEnabledTemplates() {
        templateMapper.selectEnabled().forEach(template -> {
            if (template.getPattern() != null) {
                urlPatternMap.put(template.getPattern(), template);
            } else {
                fragmentMap.put(template.getName(), template);
            }
        });
    }

    private void setTemplateHelper() {
        String sourceCode = getTemplateHelperSourceCode();
        Object o;
        try {
            Constructor<?> constructor = Compile.compile("TemplateHelper", sourceCode).getDeclaredConstructor();
            ReflectionUtils.makeAccessible(constructor);
            o = constructor.newInstance();
        } catch (Exception ex) {
            logger.warn("编译错误:" + ex.getMessage(), ex);
            o = new Object();
        }
        this.templateHelper = o;
    }

    private Object compile(String sourceCode) {
        Class<?> clazz = Compile.compile("TemplateHelper", sourceCode);
        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new LogicException("templateService.templateHelper.compile.defaultConstructor.required", "TemplateHelper需要一个无参构造器");
        }
        ReflectionUtils.makeAccessible(constructor);
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new LogicException("templateService.templateHelper.newInstance.error", "创建实例失败");
        }
    }

    private static String readResourceToString(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.toString(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void makeTemplateVisible(Template template) {
        if (template.getPattern() != null) {
            previewUrlPatternMap.remove(template.getPattern());
            urlPatternMap.put(template.getPattern(), template);
        } else {
            previewFragmentMap.remove(template.getName());
            fragmentMap.put(template.getName(), template);
        }
    }

    public static final class SystemTemplate extends Template {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final boolean regWhenFirstStartup;
        private final boolean resetAfterRemove;

        public SystemTemplate(String name, String pattern, String content, boolean regWhenFirstStartup,
                              boolean resetAfterRemove) {
            super(name, pattern, content);
            this.regWhenFirstStartup = regWhenFirstStartup;
            this.resetAfterRemove = resetAfterRemove;
        }

    }


    @Override
    public Template checkBeforeQuery(CommentModule module) {
        Template template = doCheck(module);
        Template tpl = new Template();
        tpl.setPattern(template.getPattern());
        return tpl;
    }

    @Override
    public void checkBeforeSave(Comment comment, CommentModule module) {
        Template template = doCheck(module);
        if (!BlogContext.isAuthenticated() && !template.getAllowComment()) {
            throw new LogicException("template.disableComment", "页面禁止评论");
        }
    }

    private Template doCheck(CommentModule module) {
        Optional<Template> opTemplate = templateMapper.selectById(module.getId()).filter(Template::getEnable);

        if (opTemplate.isEmpty()) {
            throw new ResourceNotFoundException("template.notExists", "模板不存在");
        }

        return opTemplate.get();
    }

    @Override
    public String getModuleName() {
        return COMMENT_MODULE_NAME;
    }

    private void checkReservedPattern(Template template) {
        if (ERROR_PATH.equals(template.getPattern())) {
            throw new LogicException("templateService.reservedPattern", "不能覆盖" + ERROR_PATH + "路径",
                    template.getPattern());
        }
    }

    public static final class CompileErrorMessage extends Message {

        private final List<Compile.CompileError> errors;

        public CompileErrorMessage(String code, String defaultMessage, List<Compile.CompileError> errors, Object... args) {
            super(code, defaultMessage, args);
            this.errors = errors;
        }

        public List<Compile.CompileError> getErrors() {
            return errors;
        }
    }

}
