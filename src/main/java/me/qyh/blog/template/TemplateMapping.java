/*
 * Copyright 2018 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;

/**
 * 用于保存模板路径
 * <p>
 * 系统有三种路径，第一种是系统固定的路径。比如控制台的路径 一种是默认的模板路径，比如首页的模板，这种模板路径可以被自定义的模板路径覆盖
 * 第三种是自定义的模板路径，其中第一第二种模板互相不影响访问，因此在实际的访问 过程中第二种的访问优先级大于第一种，在用户的自定义模板路径中，分为两种路径
 * 一种是没有包含PathVariable，直接匹配的路径，比如 login,一种是包含
 * 通配符的路径，比如space/{alias}，其中第一种路径优先级是最高的，第二种的优先级是最低的
 * </P>
 * 
 * @since 5.7
 */
public class TemplateMapping {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 系统默认路径
	 */
	private final Map<String, String> sysMap = new HashMap<>();

	/**
	 * <p>
	 * 存放默认模板路径、用户自定义覆盖的模板路径，用户自定义不包含PathVariable路径,例如:<br>
	 * login 系统默认模板路径<br>
	 * login 用户自定义模板路径(覆盖系统路径)<br>
	 * thanks 用戶自定义模板路径<br>
	 * </p>
	 */
	private final Map<String, String> highestPriorityPatternMap = new HashMap<>();

	private final EmptyPatternsMatchCondition EMPTY_PATTERNS_MATCH_CONDITION = new EmptyPatternsMatchCondition();

	private final PatternsRequestConditionHolder highestPriorityHolder = new PatternsRequestConditionHolder();

	/**
	 * 存放PathVariable类型的pattern
	 */
	private final Map<String, String> patternMap = new HashMap<>();

	private final PathMatcher pathMatcher = new AntPathMatcher();

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private final PreviewTemplateMapping previewTemplateMapping = new PreviewTemplateMapping();
	private final PatternsRequestConditionHolder holder = new PatternsRequestConditionHolder();

	public TemplateMapping() {
		super();
	}

	/**
	 * 查詢最高优先级的匹配模板
	 * 
	 * @param uncleanPath
	 * @return
	 */
	public Optional<TemplateMatch> getBestHighestPriorityTemplateMatch(String uncleanPath) {
		Objects.requireNonNull(uncleanPath);
		String path = FileUtils.cleanPath(uncleanPath);
		lock.readLock().lock();
		try {
			return doGetBestHighestPriorityTemplateMatch(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	private Optional<TemplateMatch> doGetBestHighestPriorityTemplateMatch(String path) {
		String templateName = highestPriorityPatternMap.get(path);
		if (templateName != null) {
			return Optional.of(new TemplateMatch(path, templateName));
		}

		List<String> bestMatchPatterns = highestPriorityHolder.getCondition().getMatchingPatterns(path);
		if (bestMatchPatterns.isEmpty()) {
			return Optional.empty();
		}

		String bestPattern = bestMatchPatterns.get(0);

		return Optional.of(new TemplateMatch(bestPattern, highestPriorityPatternMap.get(bestPattern)));
	}

	/**
	 * 从用户自定义的PathVariable模板路径获取最匹配的模板
	 * 
	 * @param uncleanPath
	 * @return
	 */
	public Optional<TemplateMatch> getBestPathVariableTemplateMatch(String uncleanPath) {
		Objects.requireNonNull(uncleanPath);
		String path = FileUtils.cleanPath(uncleanPath);
		lock.readLock().lock();
		try {
			return doGetBestPathVariableTemplateMatch(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	private Optional<TemplateMatch> doGetBestPathVariableTemplateMatch(String path) {
		List<String> bestMatchPatterns = holder.getCondition().getMatchingPatterns(path);
		if (bestMatchPatterns.isEmpty()) {
			return Optional.empty();
		}

		String bestPattern = bestMatchPatterns.get(0);
		return Optional.of(new TemplateMatch(bestPattern, patternMap.get(bestPattern)));
	}

	/**
	 * 注册新的路径和模板名
	 * 
	 * @param path
	 *            路径
	 * @param templateName
	 *            模板名
	 * @throws TemplatePatternAlreadyExistsException
	 *             路径已经存在
	 */
	public void register(String uncleanPattern, String templateName) throws PatternAlreadyExistsException {
		Objects.requireNonNull(uncleanPattern);
		Objects.requireNonNull(templateName);
		lock.writeLock().lock();
		try {
			String pattern = FileUtils.cleanPath(uncleanPattern);

			if (isKeyPath(pattern)) {
				throw new PatternAlreadyExistsException(pattern);
			}

			if (isHighestPriority(pattern, templateName)) {

				String highestPriorityTemplateName = highestPriorityPatternMap.get(pattern);
				if (highestPriorityTemplateName != null) {
					if (!SystemTemplate.isSystemTemplate(highestPriorityTemplateName)) {
						throw new PatternAlreadyExistsException(pattern);
					}
				}

				highestPriorityPatternMap.put(pattern, templateName);
				highestPriorityHolder.setCondition(new PatternsMatchCondition(highestPriorityPatternMap.keySet()));
			} else {

				String patternMappingTemplateName = patternMap.get(pattern);
				if (patternMappingTemplateName != null) {
					throw new PatternAlreadyExistsException(pattern);
				} else {
					List<String> matchers = holder.getCondition().getMatchingPatterns(pattern);
					if (!matchers.isEmpty()) {
						String best = matchers.get(0);
						if (this.pathMatcher.getPatternComparator(null).compare(best, pattern) == 0) {
							throw new PatternAlreadyExistsException(pattern);
						}
					}

					patternMap.put(pattern, templateName);
					holder.setCondition(new PatternsMatchCondition(patternMap.keySet()));
				}
			}

			if (SystemTemplate.isSystemTemplate(templateName)) {
				sysMap.put(pattern, templateName);
			}

		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 强制注册
	 * 
	 * @param pattern
	 * @param templateName
	 */
	public void forceRegisterTemplateMapping(String uncleanPattern, String templateName) {
		Objects.requireNonNull(uncleanPattern);
		Objects.requireNonNull(templateName);
		lock.writeLock().lock();
		try {
			String pattern = FileUtils.cleanPath(uncleanPattern);

			if (isKeyPath(pattern)) {
				return;
			}

			if (isHighestPriority(pattern, templateName)) {
				highestPriorityPatternMap.put(pattern, templateName);
				highestPriorityHolder.setCondition(new PatternsMatchCondition(highestPriorityPatternMap.keySet()));
			} else {
				if (patternMap.get(pattern) == null) {
					List<String> matchers = holder.getCondition().getMatchingPatterns(pattern);
					if (!matchers.isEmpty()) {
						String best = matchers.get(0);
						if (this.pathMatcher.getPatternComparator(null).compare(best, pattern) == 0) {
							patternMap.remove(best);
						}
					}
				}

				patternMap.put(pattern, templateName);
				holder.setCondition(new PatternsMatchCondition(patternMap.keySet()));
			}

			if (SystemTemplate.isSystemTemplate(templateName)) {
				sysMap.put(pattern, templateName);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 根据路径删除mapping
	 * <p>
	 * 如果被删除路径在系统默认路径中也能被找到，那么将会新注册一个该路径对应的系统默认路径
	 * </p>
	 * 
	 * @param pattern
	 * @return 是否成功解除注册 ，如果没有对应的映射，返回false
	 */
	public boolean unregister(String uncleanPattern) {
		Objects.requireNonNull(uncleanPattern);
		lock.writeLock().lock();
		try {
			String pattern = FileUtils.cleanPath(uncleanPattern);

			if (highestPriorityPatternMap.remove(pattern) != null) {
				String sysTemplateName = sysMap.get(pattern);
				if (sysTemplateName != null) {
					highestPriorityPatternMap.put(pattern, sysTemplateName);
				}
				highestPriorityHolder.setCondition(new PatternsMatchCondition(highestPriorityPatternMap.keySet()));
				return true;
			}
			if (patternMap.remove(pattern) != null) {
				holder.setCondition(new PatternsMatchCondition(patternMap.keySet()));
				return true;
			}
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 获取预览 PreviewTemplateMapping
	 * 
	 * @return
	 */
	public PreviewTemplateMapping getPreviewTemplateMapping() {
		return previewTemplateMapping;
	}

	public final class PreviewTemplateMapping {

		private final Map<String, PreviewTemplate> previewTemplateMap = new HashMap<>();
		private final PatternsRequestConditionHolder previewHolder = new PatternsRequestConditionHolder();

		/**
		 * 获取预览模板
		 * 
		 * @param templateName
		 * @return
		 */
		public Optional<PreviewTemplate> getPreviewTemplate(String templateName) {
			lock.writeLock().lock();
			try {
				return previewTemplateMap.values().stream()
						.filter(template -> template.getTemplateName().equals(templateName)).findAny();
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * 清空所有的预览模板
		 */
		public void clear() {
			lock.writeLock().lock();
			try {
				previewTemplateMap.clear();
				previewHolder.setCondition(EMPTY_PATTERNS_MATCH_CONDITION);
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * 根据模板名解除预览路径
		 * 
		 * @param pattern
		 */
		public void unregister(String... templateNames) {
			if (Validators.isEmpty(templateNames)) {
				return;
			}
			lock.writeLock().lock();
			try {
				out: for (Iterator<Map.Entry<String, PreviewTemplate>> it = previewTemplateMap.entrySet().iterator(); it
						.hasNext();) {
					Map.Entry<String, PreviewTemplate> entry = it.next();
					for (String templateName : templateNames) {
						String previewTemplateName = PreviewTemplate.isPreviewTemplate(templateName) ? templateName
								: PreviewTemplate.TEMPLATE_PREVIEW_PREFIX + templateName;
						if (previewTemplateName.equals(entry.getValue().getTemplateName())) {
							it.remove();
							continue out;
						}
					}
				}
				previewHolder.setCondition(new PatternsMatchCondition(previewTemplateMap.keySet()));
			} finally {
				lock.writeLock().unlock();
			}
		}

		public Optional<TemplateMatch> getBestHighestPriorityTemplateMatch(String uncleanPath) {
			Objects.requireNonNull(uncleanPath);
			String path = FileUtils.cleanPath(uncleanPath);
			lock.readLock().lock();
			try {
				if (this.previewTemplateMap.isEmpty()) {
					return doGetBestHighestPriorityTemplateMatch(path);
				}
				PatternsMatchCondition sysCondition = new PatternsMatchCondition(sysMap.keySet());
				Set<String> highestPriorityPatterns = previewTemplateMap.keySet().stream()
						.filter(pattern -> isHighestPriorityPattern(pattern, sysCondition)).collect(Collectors.toSet());
				if (!highestPriorityPatterns.isEmpty()) {
					if (highestPriorityPatterns.contains(path)) {
						return Optional.of(new TemplateMatch(path, previewTemplateMap.get(path).getTemplateName()));
					} else {
						List<String> bestMatchPatterns = new PatternsMatchCondition(highestPriorityPatterns)
								.getMatchingPatterns(path);
						if (!bestMatchPatterns.isEmpty()) {
							String bestPattern = bestMatchPatterns.get(0);
							return Optional.of(new TemplateMatch(bestPattern,
									previewTemplateMap.get(bestPattern).getTemplateName()));
						}
					}
				}

				return doGetBestHighestPriorityTemplateMatch(path);
			} finally {
				lock.readLock().unlock();
			}
		}

		public Optional<TemplateMatch> getBestPathVariableTemplateMatch(String uncleanPath) {
			Objects.requireNonNull(uncleanPath);
			String path = FileUtils.cleanPath(uncleanPath);
			lock.readLock().lock();
			try {
				if (this.previewTemplateMap.isEmpty()) {
					return doGetBestPathVariableTemplateMatch(path);
				}

				PatternsMatchCondition sysCondition = new PatternsMatchCondition(sysMap.keySet());
				Set<String> pathVariablesPatterns = previewTemplateMap.keySet().stream()
						.filter(pattern -> !isHighestPriorityPattern(pattern, sysCondition))
						.collect(Collectors.toSet());
				if (!pathVariablesPatterns.isEmpty()) {
					List<String> bestMatchPatterns = new PatternsMatchCondition(pathVariablesPatterns)
							.getMatchingPatterns(path);
					if (!bestMatchPatterns.isEmpty()) {
						String bestPattern = bestMatchPatterns.get(0);

						Optional<TemplateMatch> notPreviewTemplateMatchOptional = doGetBestPathVariableTemplateMatch(
								path);
						if (notPreviewTemplateMatchOptional.isPresent()) {
							String notPreviewBestPattern = notPreviewTemplateMatchOptional.get().getPattern();

							if (pathMatcher.getPatternComparator(null).compare(bestPattern,
									notPreviewBestPattern) > 0) {
								return notPreviewTemplateMatchOptional;
							}

						}
						return Optional.of(
								new TemplateMatch(bestPattern, previewTemplateMap.get(bestPattern).getTemplateName()));
					}
				}

				return doGetBestPathVariableTemplateMatch(path);
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * 注册一个预览路径
		 * 
		 * @param pattern
		 * @param preview
		 */
		public void register(PathTemplate ori) throws PatternAlreadyExistsException {
			Objects.requireNonNull(ori);
			lock.writeLock().lock();
			try {
				PreviewTemplate preview = wrap(ori);
				String pattern = ori.getRelativePath();
				String templateName = preview.getTemplateName();

				if (isKeyPath(pattern)) {
					throw new PatternAlreadyExistsException(pattern);
				}

				for (Iterator<Map.Entry<String, PreviewTemplate>> it = previewTemplateMap.entrySet().iterator(); it
						.hasNext();) {
					Entry<String, PreviewTemplate> entry = it.next();
					if (entry.getValue().getTemplateName().equals(templateName)) {
						it.remove();
						break;
					}
				}

				List<String> matchers = previewHolder.getCondition().getMatchingPatterns(pattern);
				if (!matchers.isEmpty()) {
					String best = matchers.get(0);
					// 如果已經存在同样匹配的路径，删除前者
					if (pathMatcher.getPatternComparator(null).compare(best, pattern) == 0) {
						previewTemplateMap.remove(best);
					}
				}

				previewTemplateMap.put(pattern, preview);
				previewHolder.setCondition(new PatternsMatchCondition(previewTemplateMap.keySet()));

			} finally {
				lock.writeLock().unlock();
			}
		}

		private PreviewTemplate wrap(PathTemplate ori) {
			if (ori instanceof PreviewTemplate) {
				return (PreviewTemplate) ori;
			}
			return new PreviewTemplate(ori);
		}

		private boolean isHighestPriorityPattern(String pattern, PatternsMatchCondition sysCondition) {
			return sysMap.containsKey(pattern) || pattern.indexOf('{') == -1
					|| !sysCondition.getMatchingPatterns(pattern).isEmpty();
		}

	}

	/**
	 * 判断路径是否是系统使用的路径
	 * 
	 * @param path
	 * @return
	 * @see RequestMappingHandlerMapping
	 */
	public boolean isKeyPath(String path) {
		if (requestMappingHandlerMapping == null) {
			return false;
		}
		if (Validators.isEmptyOrNull(path, true)) {
			return false;
		}
		if (path.equals("/")) {
			return false;
		}
		// 遍历所有的系统默认路径
		Set<String> patternSet = new HashSet<>();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> it : requestMappingHandlerMapping.getHandlerMethods()
				.entrySet()) {
			RequestMappingInfo info = it.getKey();
			PatternsRequestCondition condition = info.getPatternsCondition();
			RequestMethodsRequestCondition methodsRequestCondition = info.getMethodsCondition();
			if (methodsRequestCondition.isEmpty() || methodsRequestCondition.getMethods().contains(RequestMethod.GET)) {
				patternSet.addAll(condition.getPatterns());
			}
		}
		PatternsRequestCondition condition = new PatternsRequestCondition(
				patternSet.toArray(new String[patternSet.size()]));

		String lookupPath = "/" + path;
		return !condition.getMatchingPatterns(lookupPath).isEmpty();
	}

	private final class PatternsRequestConditionHolder {
		private PatternsMatchCondition condition;

		PatternsMatchCondition getCondition() {
			return condition == null ? EMPTY_PATTERNS_MATCH_CONDITION : condition;
		}

		void setCondition(PatternsMatchCondition condition) {
			Objects.requireNonNull(condition);
			this.condition = condition;
		}

	}

	private class EmptyPatternsMatchCondition extends PatternsMatchCondition {

		public EmptyPatternsMatchCondition() {
			super(Set.of());
		}

		@Override
		List<String> getMatchingPatterns(String lookupPath) {
			return Collections.emptyList();
		}

	}

	private class PatternsMatchCondition {
		private final Set<String> patterns;

		public PatternsMatchCondition(Set<String> patterns) {
			super();
			this.patterns = patterns;
		}

		List<String> getMatchingPatterns(String lookupPath) {
			return this.patterns.stream().map(pattern -> getMatchingPattern(pattern, lookupPath))
					.filter(Objects::nonNull).sorted(pathMatcher.getPatternComparator(lookupPath))
					.collect(Collectors.toList());
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
	}

	public final class TemplateMatch {
		private final String pattern;
		private final String templateName;

		private TemplateMatch(String pattern, String templateName) {
			super();
			this.pattern = pattern;
			this.templateName = templateName;
		}

		public String getPattern() {
			return pattern;
		}

		public String getTemplateName() {
			return templateName;
		}
	}

	private boolean isHighestPriority(String pattern, String templateName) {
		return SystemTemplate.isSystemTemplate(templateName) || sysMap.containsKey(pattern)
				|| !new PatternsMatchCondition(sysMap.keySet()).getMatchingPatterns(pattern).isEmpty()
				|| pattern.indexOf('{') == -1;
	}

	/**
	 * 获取写锁
	 * 
	 * @return
	 */
	public Lock getLock() {
		return this.lock.writeLock();
	}

}
