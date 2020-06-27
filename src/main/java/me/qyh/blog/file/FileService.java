package me.qyh.blog.file;

import me.qyh.blog.BlogContext;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.file.FileCreate.Type;
import me.qyh.blog.security.PasswordProtect;
import me.qyh.blog.security.PrivateProtect;
import me.qyh.blog.security.SecurityChecker;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 本地文件服务，提供以下功能
 * <ul>
 * <li>图片|视频文件的缩放以及获取文件信息</li>
 * <li>文件|文件夹的重命名</li>
 * <li>文件的拷贝</li>
 * <li>文件|文件夹的移动</li>
 * <li>文件|文件夹的删除</li>
 * <li>部分文件的编辑</li>
 * <li>文件保护(私有|密码)</li>
 * </ul>
 *
 * @author wwwqyhme
 */
@Component
@Conditional(value = FileCondition.class)
public class FileService {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, CountDownLatch> handleMap = new ConcurrentHashMap<>();

    private static final int MAX_NAME_LENGTH = 255;
    private static final Set<String> editableExts = Set.of("js", "css", "json", "txt", "xml", "html", "md");

    private final Semaphore sem;
    private final Path root;
    private final Path thumb;
    private final MediaTool mediaTool;
    private final FileProperties fileProperties;
    private final SecurityManager sm = new SecurityManager();

    private final String urlPrefix;

    public FileService(FileProperties fileProperties) throws IOException {
        this.fileProperties = fileProperties;
        this.mediaTool = new MediaTool(fileProperties);
        this.root = Paths.get(fileProperties.getRootPath());
        this.thumb = fileProperties.getThumbPath() == null ? null : Paths.get(fileProperties.getThumbPath());
        FileUtils.forceMkdir(this.root);
        if (this.thumb != null) {
            FileUtils.forceMkdir(this.thumb);
        }
        sem = new Semaphore(fileProperties.getTotalSem());
        if (fileProperties.getUrlPrefix() == null) {
            throw new IllegalStateException("启用了文件管理，请指定：blog.file.url-prefix");
        }
        this.urlPrefix = fileProperties.getUrlPrefix().endsWith("/") ? fileProperties.getUrlPrefix()
                : fileProperties.getUrlPrefix() + '/';
    }

    /**
     * 获取文件明细
     *
     * @param path 文件路径
     * @return 文件明细
     */
    public Optional<FileInfoDetail> getFileInfoDetail(String path) {
        this.sm.check(FileUtils.cleanPath(path));
        lock.readLock().lock();
        try {
            return lookupFile(Lookup.newLookup(path).setIgnoreRoot(false).setMustExists(true))
                    .map(this::getFileInfoDetail);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 更新文件
     *
     * @param path   要更新的文件路径
     * @param update 更新信息
     */
    public void updateFile(String path, FileUpdate update) {
        lock.writeLock().lock();
        try {
            Path p = lookupFile(Lookup.newLookup(path).setMustExists(true).setIgnoreRoot(true))
                    .orElseThrow(() -> new ResourceNotFoundException("file.notExists", "文件不存在"));

            if (!StringUtils.isNullOrBlank(update.getDirPath())) {
                Path destDir = lookupFile(Lookup.newLookup(validatePath(update.getDirPath())))
                        .orElseThrow(() -> new LogicException("fileService.move.invalidDestPath", "无效的目标文件夹路径"));
                p = move(p, destDir);
            }

            if (!StringUtils.isNullOrBlank(update.getName())) {
                p = rename(p, update.getName());
            }

            if (update.getContent() != null) {
                writeContent(p, update.getContent());
            }

            makeFileSecurity(path, update.isPrivate(), update.getPassword());

            setLastModifiedTime(p);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 创建一个的文件
     * <p>
     * <b>如果不是一个文件夹，那么它必须是可编辑的</b>
     * </p>
     *
     * @param fc 要创建的文件
     * @return 创造的文件明细
     */
    public FileInfoDetail createFile(FileCreate fc) {
        lock.writeLock().lock();
        try {
            Path file = lookupFile(Lookup.newLookup(fc.getPath()).setIgnoreRoot(true))
                    .orElseThrow(() -> new LogicException("fileService.createFile.invalidPath", "无效的文件路径"));

            if (Files.exists(file)) {
                String relative = getRelativePath(this.root, file);
                throw new LogicException("fileService.createFile.exists", relative + "已经存在文件", relative);
            }

            if (Type.DIR.equals(fc.getType())) {
                createDirectories(file);
            } else {

                if (!isEditable(FileUtils.getFileExtension(file))) {
                    throw new LogicException("fileService.createFile.uneditableFile", "无法创建无法编辑的文件");
                }

                List<Path> paths = createDirectories(file.getParent());
                try {
                    Files.createFile(file);
                } catch (IOException e) {
                    delete(paths);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            makeFileSecurity(fc.getPath(), fc.isPrivate(), fc.getPassword());

            setLastModifiedTime(file);
            return getFileInfoDetail(file);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 保存文件
     *
     * @param dirPath 文件夹位置
     * @param path    要保存的文件
     * @return 文件明细
     */
    public FileInfoDetail save(String dirPath, ReadablePath path) {
        lock.writeLock().lock();
        try {

            Path p = lookupFile(Lookup.newLookup(dirPath))
                    .orElseThrow(() -> new LogicException("fileService.save.invalidPath", "无效的文件夹路径"));

            createDirectories(p);

            String name = path.fileName();
            validSlashPath(name);

            Path dest = p.resolve(name);

            try (InputStream in = new BufferedInputStream(path.getInputStream())) {
                Files.copy(in, dest);
            } catch (FileAlreadyExistsException e) {
                String relative = getRelativePath(this.root, dest);
                throw new LogicException("fileService.upload.file.exists", "位置:" + relative + "已经存在文件", relative);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String ext = FileUtils.getFileExtension(dest);
            if (mediaTool.canHandle(ext)) {
                //read file
                try {
                    if (MediaTool.isProcessableVideo(ext)) {
                        mediaTool.readVideo(dest);
                    } else {
                        mediaTool.readImage(dest);
                    }
                } catch (Exception e) {
                    FileUtils.deleteQuietly(dest);
                    throw new LogicException("fileService.save.unreadableFile", "文件无法被读取");
                }
            }

            setLastModifiedTime(dest);

            return getFileInfoDetail(dest);
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * 分页查询本地文件
     *
     * @param param 查询条件
     * @return 分页结果
     */
    public FilePageResult query(FileQueryParam param) {
        lock.readLock().lock();
        try {
            String path = FileUtils.cleanPath(param.getPath());
            if (this.sm.needAuthenticated(path)) {
                return new FilePageResult(param, 0, List.of(), List.of());
            }
            Optional<Path> opRoot = lookupFile(Lookup.newLookup(path).setMustDir(true));

            if (opRoot.isEmpty()) {
                return new FilePageResult(param, 0, List.of(), List.of());
            }

            Path root = opRoot.get();

            List<Path> way = betweenPaths(this.root, root);
            if (!this.root.equals(root)) {
                way.add(root);
            }

            List<FileInfo> wayInfos = way.stream().map(this::getFileInfo).collect(Collectors.toList());

            FilePageResult page;
            try {
                page = doSearch(root, param);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            page.setPaths(wayInfos);
            return page;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 删除文件
     * <p>
     * <b>无法保证能够删除全部文件</b>
     * </p>
     *
     * @param path 文件路径
     */
    public void delete(String path) {
        lock.writeLock().lock();
        try {
            Path toDelete = lookupFile(Lookup.newLookup(path).setIgnoreRoot(true))
                    .orElseThrow(() -> new LogicException("fileService.delete.invalidPath", "无效的文件路径"));
            FileUtils.deleteQuietly(toDelete);

            if (!Files.exists(toDelete)) {
                afterRemove(toDelete);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取一个处理后资源，用于资源的渲染
     * <p>
     * <b>某些文件在找到后会进一步处理，例如如果找到的文件为一个视频文件，那它可能会被缩放</b>
     * </p>
     *
     * @param path        文件路径
     * @param supportWEBP 是否转化为webp
     * @return 处理后的文件
     */
    public Optional<ReadablePath> getProcessedFile(String path, boolean supportWEBP) {
        PathParser pp = new PathParser(path);
        if (pp.invalid()) {
            return Optional.empty();
        }
        this.sm.check(FileUtils.cleanPath(pp.sourcePath));
        Optional<Path> op = lookupFile(Lookup.newLookup(pp.sourcePath).setMustRegularFile(true));
        if (op.isEmpty())
            return Optional.empty();
        Path file = op.get();
        if (thumb == null) {
            return Optional.of(new _ReadablePath(file));
        }
        String ext = FileUtils.getFileExtension(file);
        Resize resize = pp.resize;
        if (resize == null || resize.isInvalid()) {
            if (MediaTool.isProcessableImage(ext) && fileProperties.isSourceProtect()) {
                return Optional.empty();
            }
        } else {
            return getThumbnail(file, resize, supportWEBP);
        }

        if (mediaTool.canHandle(ext) && MediaTool.isProcessableVideo(ext)) {
            // compress video
            Path compressed = thumb.resolve(root.relativize(file)).resolve(file.getFileName().toString() + ".mp4");
            if (Files.exists(compressed))
                return Optional.of(new _ReadablePath(compressed));
            handleFile(compressed, true, () -> {
                try {
                    mediaTool.toMP4(fileProperties.getVideoHeight(), -1, file, compressed);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });

            return Optional.of(new _ReadablePath(compressed));
        }
        return Optional.of(new _ReadablePath(file));
    }

    /**
     * 拷贝文件
     *
     * @param path    被拷贝文件路径
     * @param dirPath 目标文件夹
     */
    public FileInfoDetail copy(String path, String dirPath) {
        lock.writeLock().lock();
        try {
            Path p = lookupFile(Lookup.newLookup(path).setIgnoreRoot(true).setMustExists(true))
                    .orElseThrow(() -> new ResourceNotFoundException("file.notExists", "文件不存在"));

            Path dir = lookupFile(Lookup.newLookup(validatePath(dirPath)))
                    .orElseThrow(() -> new LogicException("fileService.copy.invalidDestPath", "无效的目标路径"));

            if (FileUtils.isSub(dir, p)) {
                throw new LogicException("fileService.copy.invalidDestPath", "无效的目标路径");
            }

            if (p.equals(dir.resolve(p.getFileName()))) {
                String relative = getRelativePath(this.root, p);
                throw new LogicException("fileService.copy.file.exists", "文件:" + relative + "已经存在", relative);
            }

            Path dest = doCopy(p, dir);
            setLastModifiedTime(dest);

            return getFileInfoDetail(dest);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 统计文件|文件夹 数目以及 文件总大小
     *
     * @return 文件统计
     */
    public FileStatistic getFileStatistic() {
        lock.readLock().lock();
        try {
            return getFileStatistic(root);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Path> createDirectories(Path dir) {
        try {
            List<Path> paths = new ArrayList<>();
            if (createAndCheckIsDirectory(dir)) {
                paths.add(dir);
            }
            return paths;
        } catch (RuntimeException ignored) {
        }

        SecurityException se = null;
        try {
            dir = dir.toAbsolutePath();
        } catch (SecurityException x) {
            se = x;
        }
        Path parent = dir.getParent();
        while (parent != null) {
            try {
                parent.getFileSystem().provider().checkAccess(parent);
                break;
            } catch (NoSuchFileException ignored) {
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            if (se == null) {
                throw new RuntimeException("Unable to determine if root directory exists:" + dir.toString());
            } else {
                throw se;
            }
        }
        List<Path> paths = new ArrayList<>();
        Path child = parent;
        for (Path name : parent.relativize(dir)) {
            child = child.resolve(name);
            try {

                if (createAndCheckIsDirectory(child)) {
                    paths.add(child);
                }

            } catch (Exception e) {
                delete(paths);
                throw e;
            }
        }
        return paths;
    }

    private synchronized boolean createAndCheckIsDirectory(Path dir) {
        try {
            Files.createDirectory(dir);
            return true;
        } catch (FileAlreadyExistsException x) {
            if (!Files.isDirectory(dir)) {
                String relative = getRelativePath(this.root, dir);
                throw new LogicException("fileService.createDir.existsFile", relative + "位置已经存在一个文件，无法创建文件夹", relative);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return false;
    }

    private void delete(List<Path> paths) {
        paths.forEach(FileUtils::deleteQuietly);
    }

    private String validatePath(String path) {
        if (!StringUtils.isNullOrBlank(path)) {
            path = FileUtils.cleanPath(path);
            if (!StringUtils.isNullOrBlank(path)) {

                if (path.indexOf('/') > -1) {
                    for (String _path : path.split("/")) {
                        validSlashPath(_path);
                    }
                } else {
                    validSlashPath(path);
                }

                return path;
            }
        }
        return "";
    }

    private void validSlashPath(String path) {
        if (!FileUtils.maybeValidateFilename(path)) {
            throw new LogicException("fileService.name.valid", "无效的文件名:" + path, path);
        }
        if (path.length() > MAX_NAME_LENGTH) {
            throw new LogicException("fileService.name.toolong", "文件名不能超过" + MAX_NAME_LENGTH + "个字符", MAX_NAME_LENGTH);
        }
    }

    private Optional<Path> lookupFile(Lookup lookup) {
        Path p;
        try {
            p = resolve(root, lookup.path);
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
        if (!FileUtils.isSub(p, root))
            return Optional.empty();
        if (lookup.mustExists && !Files.exists(p))
            return Optional.empty();
        if (lookup.ignoreRoot && p == root)
            return Optional.empty();
        if (lookup.mustRegularFile && !Files.isRegularFile(p))
            return Optional.empty();
        if (lookup.mustDir && !Files.isDirectory(p))
            return Optional.empty();
        return Optional.of(p);
    }

    private Path resolve(Path root, String path) {
        Path resolve;
        if (StringUtils.isNullOrBlank(path)) {
            resolve = root;
        } else {
            String cpath = FileUtils.cleanPath(path);
            resolve = StringUtils.isNullOrBlank(cpath) ? root : root.resolve(cpath);
        }
        return resolve;
    }

    protected Path doCopy(Path source, Path dest) {
        List<Path> rollBacks = Collections.synchronizedList(createDirectories(dest));
        Path root = dest.resolve(source.getFileName());
        try {
            Files.walk(source).filter(Files::isRegularFile).parallel().forEach(path -> {
                Path target = root.resolve(source.relativize(path));
                boolean exists = Files.exists(target);
                try {
                    rollBacks.addAll(createDirectories(target.getParent()));
                    Files.copy(path, target);
                    rollBacks.add(target);
                } catch (Exception e) {
                    if (!exists) {
                        FileUtils.deleteQuietly(target);
                    }
                    delete(rollBacks);
                    if (e instanceof FileAlreadyExistsException) {
                        String relative = getRelativePath(this.root, dest);
                        throw new LogicException("fileService.copy.file.exists", "文件:" + relative + "已经存在",
                                relative);
                    }
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
            return root;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected boolean isEditable(String ext) {
        return editableExts.stream().anyMatch(p -> p.equalsIgnoreCase(ext));
    }

    private void handleFile(Path file, boolean video, Runnable runnable) {
        String key = file.toString();
        CountDownLatch cdl = new CountDownLatch(1);
        CountDownLatch current = handleMap.putIfAbsent(key, cdl);
        if (current != null) {
            try {
                current.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return;
        }
        int semNum = video ? fileProperties.getVideoSemPer() : fileProperties.getImageSemPer();
        boolean releaseSem = true;
        try {
            sem.acquire(semNum);
            try {
                runnable.run();
            } finally {
                cdl.countDown();
            }
        } catch (InterruptedException e) {
            releaseSem = false;
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (releaseSem) {
                sem.release(semNum);
            }
            handleMap.remove(key);
        }
    }

    private List<Path> betweenPaths(Path root, Path dir) {
        if (root.equals(dir)) {
            return new ArrayList<>();
        }
        Path parent = dir;
        List<Path> paths = new ArrayList<>();
        while ((parent = parent.getParent()) != null) {
            if (parent.equals(root)) {
                if (!paths.isEmpty()) {
                    Collections.reverse(paths);
                }
                return paths;
            }
            paths.add(parent);
        }
        throw new RuntimeException("无法找出两个path之间的路径");
    }

    private FileInfo getFileInfo(Path path) {
        FileInfo fi = new FileInfo();
        fi.setDir(Files.isDirectory(path));
        fi.setName(path.getFileName().toString());
        try {
            FileTime ft = Files.getLastModifiedTime(path);
            fi.setLastModify(LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault()));
        } catch (IOException ignored) {
        }
        if (!fi.isDir()) {
            fi.setExt(FileUtils.getFileExtension(fi.getName()));
            fi.setEditable(isEditable(fi.getExt()));
        }
        fi.setPath(getRelativePath(this.root, path));
        fi.setUrl(this.urlPrefix + fi.getPath());
        List<SecurityType> types = this.sm.getSecurityTypes(fi.getPath());
        for (SecurityType type : types) {
            if (type.equals(SecurityType.PRIVATE)) {
                fi.setPrivate(true);
            } else {
                fi.setProtected(true);
            }
        }
        if (!fi.isDir() && thumb != null && mediaTool.canHandle(fi.getExt())
                && (MediaTool.isProcessableImage(fi.getExt()) || MediaTool.isProcessableVideo(fi.getExt()))) {
            Path smallThumbFile = getThumbPath(path, new Resize(fileProperties.getSmallThumbSize()), false);
            fi.setSmallThumbPath(getRelativePath(thumb, smallThumbFile));
            fi.setSmallThumbUrl(this.urlPrefix + fi.getSmallThumbPath());

            Path middleThumbFile = getThumbPath(path, new Resize(fileProperties.getMiddleThumbSize()), false);
            fi.setMiddleThumbPath(getRelativePath(thumb, middleThumbFile));
            fi.setMiddleThumbUrl(this.urlPrefix + fi.getMiddleThumbPath());

            Path largeThumbFile = getThumbPath(path, new Resize(fileProperties.getLargeThumbSize()), false);
            fi.setLargeThumbPath(getRelativePath(thumb, largeThumbFile));
            fi.setLargeThumbUrl(this.urlPrefix + fi.getLargeThumbPath());
        }
        return fi;
    }

    protected FilePageResult doSearch(Path root, FileQueryParam param) throws IOException {
        boolean needQuery = !CollectionUtils.isEmpty(param.getExtensions())
                || !StringUtils.isNullOrBlank(param.getName());
        Predicate<Path> predicate = p -> {
            if (!p.equals(root)) {
                String path = getRelativePath(this.root, p);
                return !this.sm.needAuthenticated(path);
            }
            return false;
        };
        if (needQuery) {
            predicate = predicate.and(p -> matchParam(param, p.getFileName().toString()));
        }
        if (!param.isSortByLastModify()) {
            if (param.isIgnorePaging()) {
                List<FileInfo> files = Files.walk(root, param.isQuerySubDir() ? Integer.MAX_VALUE : 1).filter(predicate)
                        .map(this::getFileInfo).collect(Collectors.toList());
                return new FilePageResult(param, files.size(), files);
            }
            List<FileInfo> files = Files.walk(root, param.isQuerySubDir() ? Integer.MAX_VALUE : 1).filter(predicate)
                    .skip(param.getOffset()).limit(param.getPageSize()).map(this::getFileInfo)
                    .collect(Collectors.toList());
            int count = (int) Files.walk(root, param.isQuerySubDir() ? Integer.MAX_VALUE : 1).filter(predicate).count();
            return new FilePageResult(param, count, files);
        }

        Pair[] pairs = Files.walk(root, param.isQuerySubDir() ? Integer.MAX_VALUE : 1).filter(predicate).map(Pair::new)
                .toArray(Pair[]::new);

        Arrays.sort(pairs);

        int total = pairs.length;

        if (param.isIgnorePaging()) {
            return new FilePageResult(param, total,
                    Arrays.stream(pairs).map(p -> p.f).map(this::getFileInfo).collect(Collectors.toList()));
        }

        if (param.getOffset() >= total) {
            return new FilePageResult(param, total, List.of());
        }

        int to = Math.min(total, param.getOffset() + param.getPageSize());

        List<FileInfo> files = new ArrayList<>();
        for (int i = param.getOffset(); i < to; i++) {
            files.add(getFileInfo(pairs[i].f));
        }
        return new FilePageResult(param, total, files);
    }

    private boolean matchParam(FileQueryParam param, String name) {
        String ext = FileUtils.getFileExtension(name);
        if (!CollectionUtils.isEmpty(param.getExtensions())
                && param.getExtensions().stream().noneMatch(ex -> ex.equalsIgnoreCase(ext))) {
            return false;
        }
        String mName = param.getName();
        return StringUtils.isNullOrBlank(mName) || name.contains(mName);
    }

    private Path getThumbDir(Path file) {
        return thumb.resolve(root.relativize(file));
    }

    private Path getThumbPath(Path file, Resize resize, boolean toWEBP) {
        // find exits thumb file
        Path thumbDir = getThumbDir(file);
        String name = resize.toString();
        return thumbDir.resolve(toWEBP ? name + ".WEBP" : name);
    }

    protected Path move(Path p, Path destDir) {

        if (p.equals(destDir)) {
            return destDir;
        }

        Path dest = destDir.resolve(p.getFileName());

        if (p.equals(dest)) {
            return dest;
        }

        if (Files.exists(dest)) {
            String relative = getRelativePath(this.root, dest);
            throw new LogicException("fileService.move.file.exists", "文件:" + relative + "已经存在", relative);
        }

        if (FileUtils.isSub(destDir, p)) {
            throw new LogicException("fileService.move.invalidDestPath", "无效的目标文件夹路径");
        }

        List<Path> rollBacks = new ArrayList<>();
        try {
            rollBacks.addAll(createDirectories(destDir));
            Files.move(p, dest, StandardCopyOption.ATOMIC_MOVE);
            afterRemove(p);
            return dest;
        } catch (Exception e) {
            delete(rollBacks);
            if (e instanceof LogicException) {
                throw (LogicException) e;
            }
            if (e instanceof FileAlreadyExistsException) {
                String relative = getRelativePath(this.root, destDir.resolve(p.getFileName()));
                throw new LogicException("fileService.move.file.exists", "文件:" + relative + "已经存在", relative);
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected Path rename(Path p, String newName) {
        String finalName = newName;
        // 如果重命名文件，不应该改变文件后缀
        if (Files.isRegularFile(p)) {
            String ext = FileUtils.getFileExtension(p);
            if (!ext.isEmpty()) {
                finalName += "." + ext;
            }
        }

        validSlashPath(finalName);
        Path dest = this.root.resolve(p.getParent()).resolve(finalName);

        if (!FileUtils.isSub(dest, this.root)) {
            throw new LogicException("fileService.rename.invalidPath", "无效的文件路径");
        }

        if (p.equals(dest)) {
            return dest;
        }

        if (Files.exists(dest)) {
            String relative = getRelativePath(this.root, dest);
            throw new LogicException("fileService.rename.dest.exists", "文件:" + relative + "已经存在", relative);
        }
        List<Path> rollBacks = new ArrayList<>();
        try {
            rollBacks.addAll(createDirectories(dest.getParent()));
            Files.move(p, dest, StandardCopyOption.ATOMIC_MOVE);
            afterRemove(p);
            return dest;
        } catch (Exception e) {
            delete(rollBacks);
            if (e instanceof LogicException) {
                throw (LogicException) e;
            }
            if (e instanceof FileAlreadyExistsException) {
                String relative = getRelativePath(this.root, dest);
                throw new LogicException("fileService.rename.dest.exists", "文件:" + relative + "已经存在", relative);
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void writeContent(Path file, String content) {
        if (!isEditable(FileUtils.getFileExtension(file))) {
            throw new LogicException("fileService.edit.unable", "文件不能被编辑");
        }
        try {
            Files.writeString(file, content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getRelativePath(Path root, Path file) {
        return FileUtils.cleanPath(root.relativize(file).toString());
    }

    private void afterRemove(Path p) {
        if (thumb != null) {
            FileUtils.deleteQuietly(getThumbDir(p));
        }
        this.sm.removeSecurityPath(getRelativePath(this.root, p));
    }

    private FileStatistic getFileStatistic(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new FileStatistic(0, 0, 0, null);
        }
        LongAdder lengthAdder = new LongAdder();
        LongAdder dirAdder = new LongAdder();
        LongAdder filesAdder = new LongAdder();
        Map<String, TypeAdder> typeGroupingAdder = new ConcurrentHashMap<>();
        Files.walk(path).parallel().forEach(p -> {
            if (p == path) {
                return;
            }
            if (Files.isRegularFile(p)) {
                filesAdder.add(1);
                long size = FileUtils.size(p);
                String type = FileUtils.getFileExtension(p).toLowerCase();
                TypeAdder typeAdder = typeGroupingAdder.computeIfAbsent(type, k -> new TypeAdder());
                typeAdder.countAdder.add(1);
                typeAdder.sizeAdder.add(size);
                lengthAdder.add(size);
            }
            if (Files.isDirectory(p)) {
                dirAdder.add(1);
            }
        });

        List<FileTypeStatistic> typeStatistics = new ArrayList<>();
        if (!typeGroupingAdder.isEmpty()) {
            for (Map.Entry<String, TypeAdder> it : typeGroupingAdder.entrySet()) {
                TypeAdder typeAdder = it.getValue();
                typeStatistics.add(new FileTypeStatistic(it.getKey(), typeAdder.sizeAdder.longValue(),
                        typeAdder.countAdder.longValue()));
            }
        }
        return new FileStatistic(lengthAdder.longValue(), dirAdder.longValue(), filesAdder.longValue(), typeStatistics);
    }

    private FileInfoDetail getFileInfoDetail(Path file) {
        FileInfoDetail fid = new FileInfoDetail(getFileInfo(file));
        if (fid.isEditable()) {
            // set file content
            try {
                fid.setContent(Files.readString(file));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        fid.setProperties(getFileProperties(file));
        return fid;
    }

    private Map<String, Object> getFileProperties(Path p) {
        Map<String, Object> propertiesMap = new LinkedHashMap<>();

        if (Files.isDirectory(p)) {
            try {
                FileStatistic fs = getFileStatistic(p);
                propertiesMap.put("size", fs.getSize());
                propertiesMap.put("dirCount", fs.getDirCount());
                propertiesMap.put("fileCount", fs.getFileCount());
                if (fs.getTypeStatistics() != null) {
                    propertiesMap.put("typeStatistics", fs.getTypeStatistics());
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        if (Files.isRegularFile(p)) {
            propertiesMap.put("size", FileUtils.size(p));
            String ext = FileUtils.getFileExtension(p);
            if (mediaTool.canHandle(ext)) {
                if (MediaTool.isProcessableVideo(ext)) {
                    try {
                        VideoInfo vi = mediaTool.readVideo(p);
                        propertiesMap.put("width", vi.getWidth());
                        propertiesMap.put("height", vi.getHeight());
                        propertiesMap.put("duration", vi.getDuration());
                    } catch (IOException ignored) {
                    }
                }
                if (MediaTool.isProcessableImage(ext)) {
                    try {
                        ImageInfo ii = mediaTool.readImage(p);
                        propertiesMap.put("width", ii.getWidth());
                        propertiesMap.put("height", ii.getHeight());
                        propertiesMap.put("type", ii.getType());
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return propertiesMap;
    }

    private static final class Pair implements Comparable<Pair> {
        private final long t;
        private final Path f;

        public Pair(Path file) {
            f = file;
            t = FileUtils.lastModified(file);
        }

        public int compareTo(Pair o) {
            return Long.compare(o.t, t);
        }
    }

    private static final class TypeAdder {
        private final LongAdder countAdder = new LongAdder();
        private final LongAdder sizeAdder = new LongAdder();
    }

    public enum SecurityType {
        PRIVATE, PASSWORD
    }

    private final class SecurityManager {
        private final Path authFile = Paths.get(System.getProperty("user.home"))
                .resolve("blog/file_service_auth_path.properties");

        private final Map<String, String> securityPaths = new ConcurrentHashMap<>();

        public SecurityManager() {
            super();
            if (Files.exists(authFile)) {
                Properties pros;
                try {
                    pros = PropertiesLoaderUtils.loadProperties(new FileSystemResource(authFile));
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                pros.keySet().stream().map(Object::toString).forEach(k -> {
                    String v = pros.getProperty(k);
                    securityPaths.put(k, v);
                });
            }
        }

        public List<SecurityType> getSecurityTypes(String path) {
            List<SecurityType> types = new ArrayList<>();
            securityPaths.forEach((k, v) -> {
                if (k.equals(path) || path.startsWith(k + "/")) {
                    types.add(v.isEmpty() ? SecurityType.PRIVATE : SecurityType.PASSWORD);
                }
            });
            return types;
        }

        public void removeSecurityPath(String path) {
            String clean = FileUtils.cleanPath(path);
            securityPaths.keySet().removeIf(k -> k.equals(clean) || k.startsWith(clean + "/"));
            save();
        }

        public void makePathSecurity(String path, String password) {
            lookupFile(Lookup.newLookup(path).setMustExists(true))
                    .orElseThrow(() -> new ResourceNotFoundException("file.notExists", "文件不存在"));
            String clean = FileUtils.cleanPath(path);
            securityPaths.forEach((k, v) -> {
                if (clean.startsWith(k + "/")) {
                    if (!v.isEmpty() && !password.isEmpty()) {
                        throw new LogicException("securityPath.protected", "路径:" + k + "已经设置密码保护了", k);
                    }
                    if (v.isEmpty()) {
                        throw new LogicException("securityPath.private", "路径:" + k + "已经私有化了", k);
                    }
                }
                if (k.startsWith(clean + "/") && (!v.isEmpty() || password.isEmpty())) {
                    securityPaths.remove(k);
                }
            });
            securityPaths.put(clean, password);
            save();
        }

        public void check(String path) {
            if (BlogContext.isAuthenticated()) {
                return;
            }
            securityPaths.forEach((k, v) -> {
                if (k.equals(path) || path.startsWith(k + "/")) {
                    SecurityChecker.check(new SecurityPath(k, v));
                }
            });
        }

        public boolean needAuthenticated(String path) {
            if (BlogContext.isAuthenticated()) {
                return false;
            }
            return securityPaths.keySet().stream().anyMatch(k -> k.equals(path) || path.startsWith(k + "/"));
        }

        private synchronized void save() {
            FileUtils.createFile(authFile);
            Properties pros = new Properties();
            securityPaths.forEach(pros::put);
            try (OutputStream out = Files.newOutputStream(authFile)) {
                pros.store(out, null);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 获取目标文件的缩略图文件
     * <p>
     * <b>如果目标文件是可被处理的视频文件，那么将返回视频的封面的缩略图</b>
     * </p>
     *
     * @param file        文件路径
     * @param resize      缩略图尺寸信息
     * @param supportWEBP 是否转化为webp文件
     * @return 缩放后的文件
     */
    private Optional<ReadablePath> getThumbnail(Path file, Resize resize, boolean supportWEBP) {
        // valid resize
        Integer size = resize.getSize();
        if (!BlogContext.isAuthenticated() && (size == null || (size != fileProperties.getSmallThumbSize()
                && size != fileProperties.getMiddleThumbSize() && size != fileProperties.getLargeThumbSize()
                && Arrays.stream(fileProperties.getImageSizes()).noneMatch(allowSize -> allowSize == size))))
            return Optional.empty();
        String ext = FileUtils.getFileExtension(file);
        boolean toWEBP = supportWEBP && (MediaTool.isJPEG(ext) || MediaTool.isPNG(ext));
        Path thumbFile = getThumbPath(file, resize, toWEBP);
        String sourceExt = toWEBP ? MediaTool.WEBP : MediaTool.isProcessableVideo(ext) ? MediaTool.PNG : ext;

        if (Files.exists(thumbFile)) {
            return Optional.of(new ThumbnailReadablePath(thumbFile, sourceExt));
        }

        if (!mediaTool.canHandle(ext))
            return Optional.empty();

        Path dest = null;
        if (MediaTool.isProcessableVideo(ext)) {
            // find poster of video
            Path poster = thumb.resolve(root.relativize(file))
                    .resolve(FileUtils.getNameWithoutExtension(file.getFileName().toString()) + "@." + MediaTool.PNG);

            if (!Files.exists(poster)) {
                handleFile(poster, false, () -> {
                    try {
                        mediaTool.getVideoPoster(file, poster);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            dest = poster;
        } else if (MediaTool.isProcessableImage(ext)) {
            dest = file;
        }
        if (dest == null)
            return Optional.empty();
        // check again
        if (!mediaTool.canHandle(FileUtils.getFileExtension(dest)))
            return Optional.empty();
        final Path source = dest;
        handleFile(dest, false, () -> {
            try {
                mediaTool.resizeImage(resize, source, thumbFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return Optional.of(new ThumbnailReadablePath(thumbFile, sourceExt));
    }

    private void makeFileSecurity(String path, boolean isPrivate, String password) {
        this.sm.removeSecurityPath(path);

        if (isPrivate) {
            this.sm.makePathSecurity(path, "");
        } else {
            if (!StringUtils.isNullOrBlank(password)) {
                this.sm.makePathSecurity(path, password);
            }
        }
    }

    /**
     * @since 9.0
     */
    private void setLastModifiedTime(Path f) {
        List<Path> paths = betweenPaths(this.root, f);
        paths.add(f);
        FileTime now = FileTime.fromMillis(System.currentTimeMillis());
        for (Path p : paths) {
            try {
                Files.setLastModifiedTime(p, now);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * <ul>
     * <li>123.png=>empty</li>
     * <li>123.png/200=>new Resize(200)</li>
     * <li>123.png/200x300=>new Resize(200,300,true)</li>
     * <li>123.png/200x300!=>new Resize(200,300,false)</li>
     * <li>123.png/x300=>new Resize(null,300,true)</li>
     * <li>123.png/200x=>new Resize(200,null,true)</li>
     * </ul>
     */
    private static final class PathParser {

        private static final String CONCAT = "x";
        private static final String FORCE = "!";
        private static final Resize INVALID_RESIZE = new Resize(-1);

        private final String sourcePath;
        private final Resize resize;
        private final String path;

        public PathParser(String path) {
            super();
            this.path = path;
            this.resize = getResizeFromPath(path);
            this.sourcePath = getSourcePathByResizePath(path);
        }

        public boolean invalid() {
            if (this.resize == null || this.resize == INVALID_RESIZE) {
                return !this.path.equals(this.sourcePath);
            }
            return this.resize.isInvalid();
        }

        private String getSourcePathByResizePath(String path) {
            String ext = FileUtils.getFileExtension(path);
            if (!ext.strip().isEmpty())
                return path;
            String sourcePath = path;
            int index = path.lastIndexOf('/');
            if (index != -1) {
                sourcePath = path.substring(0, index);
            }
            return sourcePath;
        }

        private Resize getResizeFromPath(String path) {

            String ext = FileUtils.getFileExtension(path);
            if (!ext.strip().isEmpty())
                return null;

            String name = FileUtils.getNameWithoutExtension(path);
            if (!name.contains(CONCAT))
                try {
                    return new Resize(Integer.parseInt(name));
                } catch (NumberFormatException e) {
                    return INVALID_RESIZE;
                }
            // only height
            if (name.startsWith(CONCAT))
                try {
                    return new Resize(null, Integer.parseInt(name.substring(1)), true);
                } catch (NumberFormatException e) {
                    return INVALID_RESIZE;
                }
            // only width
            if (name.endsWith(CONCAT))
                try {
                    return new Resize(Integer.parseInt(name.substring(0, name.length() - 1)), null, true);
                } catch (NumberFormatException e) {
                    return INVALID_RESIZE;
                }
            boolean keepRatio = !name.endsWith(FORCE);
            String sizeInfo = keepRatio ? name : name.substring(0, name.length() - 1);
            // both
            String[] array = sizeInfo.split(CONCAT);
            if (array.length != 2)
                return INVALID_RESIZE;
            try {
                return new Resize(Integer.parseInt(array[0]), Integer.parseInt(array[1]), keepRatio);
            } catch (NumberFormatException e) {
                return INVALID_RESIZE;
            }
        }

    }

    private static final class SecurityPath implements PasswordProtect, PrivateProtect {
        private final String path;
        private final String password;

        public SecurityPath(String path, String password) {
            super();
            this.path = path;
            this.password = password;
        }

        @Override
        public String getResId() {
            return "file-" + path;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isHasPassword() {
            return !password.isEmpty();
        }

        @Override
        public Boolean getIsPrivate() {
            return password.isEmpty();
        }
    }

    private static final class Lookup {
        private final String path;

        private boolean mustExists;
        private boolean mustRegularFile;
        private boolean mustDir;
        private boolean ignoreRoot;

        public Lookup(String path) {
            super();
            this.path = path;
        }

        public static Lookup newLookup(String path) {
            return new Lookup(path);
        }

        public Lookup setMustExists(boolean mustExists) {
            this.mustExists = mustExists;
            return this;
        }

        public Lookup setMustDir(boolean mustDir) {
            this.mustDir = mustDir;
            return this;
        }

        public Lookup setMustRegularFile(boolean mustRegularFile) {
            this.mustRegularFile = mustRegularFile;
            return this;
        }

        public Lookup setIgnoreRoot(boolean ignoreRoot) {
            this.ignoreRoot = ignoreRoot;
            return this;
        }

    }

    private static class _ReadablePath implements ReadablePath {
        private final Path path;

        public _ReadablePath(Path path) {
            super();
            this.path = path;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public String fileName() {
            return path.getFileName().toString();
        }

        @Override
        public String getExtension() {
            return FileUtils.getFileExtension(path);
        }

        @Override
        public long size() {
            return FileUtils.size(path);
        }

        @Override
        public long lastModified() {
            return FileUtils.lastModified(path);
        }
    }

    private static class ThumbnailReadablePath extends _ReadablePath {

        private final String sourceExt;

        public ThumbnailReadablePath(Path path, String sourceExt) {
            super(path);
            this.sourceExt = sourceExt;
        }

        @Override
        public String getExtension() {
            return sourceExt;
        }
    }
}
