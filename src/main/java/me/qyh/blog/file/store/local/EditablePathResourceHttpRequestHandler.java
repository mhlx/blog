/*
 * Copyright 2017 qyh.me
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
package me.qyh.blog.file.store.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.PathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.file.vo.FileContent;
import me.qyh.blog.file.vo.StaticFile;
import me.qyh.blog.file.vo.StaticFilePageResult;
import me.qyh.blog.file.vo.StaticFileQueryParam;
import me.qyh.blog.file.vo.StaticFileStatistics;
import me.qyh.blog.file.vo.StaticFileUpload;
import me.qyh.blog.file.vo.UnzipConfig;
import me.qyh.blog.file.vo.UploadedFile;

/**
 * 一个可对文件进行管理的 ResourceHttpRequestHandler
 * 
 * @since 5.7
 */
public class EditablePathResourceHttpRequestHandler extends CustomResourceHttpRequestHandler {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private final Logger logger = LoggerFactory.getLogger(EditablePathResourceHttpRequestHandler.class);

	private static final String ZIP = "zip";
	private static final String MALFORMED = "MALFORMED";

	private Path root;
	private String prefix;

	private static final int MAX_NAME_LENGTH = 255;

	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private ContentNegotiationManager contentNegotiationManager;

	private static final Set<String> editableExts = Set.of("js", "css", "json", "txt", "xml");

	/**
	 * @param rootLocation
	 *            根目录位置
	 * @param prefix
	 *            访问链接前缀 见 &lt;mvc:resources/&gt;
	 */
	public EditablePathResourceHttpRequestHandler(String rootLocation, String prefix) {
		this(Paths.get(rootLocation), prefix);
	}

	/**
	 * 
	 * @param root
	 *            根目录
	 * @param prefix
	 *            访问前缀
	 */
	public EditablePathResourceHttpRequestHandler(Path root, String prefix) {
		this.init(root, prefix);
	}

	protected EditablePathResourceHttpRequestHandler() {
		super();
	}

	protected void init(Path root, String prefix) {
		Objects.requireNonNull(root);
		Objects.requireNonNull(prefix);
		if (root.getParent() == null) {
			throw new SystemException("不能以根目录作为存储位置");
		}

		if (FileUtils.isRegularFile(root)) {
			throw new SystemException("根目录不能是文件");
		}
		this.root = root;
		this.prefix = FileUtils.cleanPath(prefix);
		if (this.prefix.isEmpty()) {
			throw new SystemException("访问前缀不能为空");
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param upload
	 * @return
	 * @throws LogicException
	 */
	public List<UploadedFile> upload(StaticFileUpload upload) throws LogicException {
		lock.writeLock().lock();
		try {

			Path p = this.root.resolve(validatePath(upload.getPath()));

			if (!FileUtils.isSub(p, root)) {
				throw new LogicException("staticFile.upload.dir.notInRoot", "文件上传存储目录不在根目录内");
			}

			createDirectories(p);

			List<UploadedFile> results = new ArrayList<>();

			for (MultipartFile file : upload.getFiles()) {
				String name = file.getOriginalFilename();

				try {
					validSlashPath(name);
				} catch (LogicException e) {
					results.add(new UploadedFile(name, e.getLogicMessage()));
					continue;
				}

				Path dest = p.resolve(name);

				try (InputStream in = new BufferedInputStream(file.getInputStream())) {

					Files.copy(in, dest);
					results.add(new UploadedFile(name, file.getSize(), null, null));

				} catch (FileAlreadyExistsException e) {
					Path relative = this.root.relativize(dest);
					results.add(new UploadedFile(name,
							new Message("staticFile.upload.file.exists", "位置:" + relative + "已经存在文件", relative)));
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					results.add(new UploadedFile(name, Constants.SYSTEM_ERROR));
					// throw new SystemException(e.getMessage(), e);
				}

			}

			return results;

		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 解压缩文件
	 * 
	 * @param path
	 *            压缩文件路径
	 * @param destPath
	 *            存放目录
	 * @param config
	 *            配置
	 * @throws LogicException
	 */
	public void unzip(String zipPath, UnzipConfig config) throws LogicException {
		lock.writeLock().lock();
		try {
			Path zip = byPath(validatePath(zipPath));
			if (!FileUtils.isRegularFile(zip) || !ZIP.equalsIgnoreCase(FileUtils.getFileExtension(zip))) {
				Path relative = this.root.relativize(zip);
				throw new LogicException("staticFile.unzip.notZipFile", "文件:" + relative + "不是zip文件", relative);
			}

			Path dest = root.resolve(validatePath(config.getPath()));
			if (!FileUtils.isSub(dest, root)) {
				throw new LogicException("staticFile.unzip.dest.notInRoot", "解压缩位置不在根目录内");
			}

			try {
				doUnzip(zip, dest, config);
			} catch (IllegalArgumentException e) {
				String msg = e.getMessage();
				if (msg.contains(MALFORMED)) {
					throw new LogicException("staticFile.unzip.path.unread", "zip文件中某个路径无法被读取，可能字符不符");
				}
				throw e;
			}

			if (config.isDeleteAfterSuccessUnzip()) {
				FileUtils.deleteQuietly(zip);
			}

		} finally {
			lock.writeLock().unlock();
		}
	}

	private void doUnzip(Path zip, Path dir, UnzipConfig config) throws LogicException {

		Charset charset = null;
		if (!Validators.isEmptyOrNull(config.getEncoding(), true)) {
			try {
				charset = Charset.forName(config.getEncoding());
			} catch (Exception e) {

			}
		}

		if (charset == null) {
			charset = StandardCharsets.UTF_8;
		}

		List<Path> rollbacks = new ArrayList<>();
		try (ZipFile zipFile = new ZipFile(zip.toFile(), charset)) {

			List<ZipEntry> entryList = new ArrayList<>();
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					String name = entry.getName();
					validatePath(name);

					Path dest = dir.resolve(name);

					if (Files.exists(dest)) {
						Path relative = this.root.relativize(dest);
						throw new LogicException("staticFile.unzip.file.exists", "位置:" + relative + "已经存在文件", relative);
					}

					rollbacks.addAll(createDirectories(dest.getParent()));

					entryList.add(entry);
				}
			}

			entryList.parallelStream().forEach(entry -> {
				Path dest = dir.resolve(entry.getName());
				try (InputStream is = new BufferedInputStream(zipFile.getInputStream(entry))) {
					Files.copy(is, dest);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});

		} catch (LogicException e) {
			delete(rollbacks);
			throw e;
		} catch (Exception e) {
			delete(rollbacks);

			if (e instanceof ZipException) {
				throw new LogicException("staticFile.unzip.broken", "zip文件损坏或者不是正确的格式");
			}

			throw new SystemException(e.getMessage(), e);
		}
	}

	/**
	 * 创建文件夾，如果创建失败。将会删除已经创建的文件夹
	 * 
	 * @param path
	 * @return 新建的文件夹，不包含本来已经存在的文件夹
	 * @throws LogicException
	 *             如果文件已经存在但是不是一个文件夹
	 */
	private List<Path> createDirectories(Path dir) throws LogicException {
		try {
			List<Path> paths = new ArrayList<>();
			if (createAndCheckIsDirectory(dir)) {
				paths.add(dir);
			}
			return paths;
		} catch (SystemException x) {
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
			} catch (NoSuchFileException x) {
			} catch (IOException ex) {
				throw new SystemException(ex.getMessage(), ex);
			}
			parent = parent.getParent();
		}
		if (parent == null) {
			if (se == null) {
				throw new SystemException("Unable to determine if root directory exists:" + dir.toString());
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

	/**
	 * 创建一个文件夹
	 * 
	 * @param dir
	 * @return 是否创建了一个新的文件夹
	 * @throws IOException
	 *             创建文件夹失败
	 */
	private synchronized boolean createAndCheckIsDirectory(Path dir) throws LogicException {
		try {
			Files.createDirectory(dir);
			return true;
		} catch (FileAlreadyExistsException x) {
			if (!Files.isDirectory(dir)) {
				Path relative = this.root.relativize(dir);
				throw new LogicException("staticFile.createDir.file.exists",
						"创建文件夹失败，位置:" + relative + "已经存在文件，但不是一个文件夹", relative);
			}
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		return false;
	}

	private Path byPath(String path) throws LogicException {
		Path p = resolve(root, path);
		if (!FileUtils.isSub(p, root)) {
			throw new LogicException("staticFile.notInRoot", "文件不在根目录内");
		}
		if (!FileUtils.exists(p)) {
			Path relative = root.relativize(p);
			throw new LogicException("staticFile.notExists", "文件" + relative + "不存在", relative);
		}
		return p;
	}

	/**
	 * 分页查询本地文件
	 * 
	 * @param param
	 * @return
	 */
	public StaticFilePageResult query(StaticFileQueryParam param) {
		lock.readLock().lock();
		try {
			Path root = resolve(this.root, param.getPath());

			if (!FileUtils.exists(root) || FileUtils.isRegularFile(root) || !FileUtils.isSub(root, this.root)) {
				return new StaticFilePageResult(new ArrayList<>(), new PageResult<>(param, 0, new ArrayList<>()));
			}

			List<Path> way = betweenPaths(this.root, root);
			if (!this.root.equals(root)) {
				way.add(root);
			}

			List<StaticFile> transferWay = way.stream().map(this::toStaticFile).collect(Collectors.toList());

			PageResult<StaticFile> page;
			try {
				page = param.isQuerySubDir() ? doWalkSearch(root, param) : doSubSearch(root, param);
			} catch (IOException e) {
				throw new SystemException(e.getMessage(), e);
			}

			return new StaticFilePageResult(transferWay, page);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 查询文件夹下第一层子文件
	 * 
	 * @param root
	 *            根目录
	 * @param param
	 *            查询参数
	 * @return
	 * @throws IOException
	 */
	protected PageResult<StaticFile> doSubSearch(Path root, StaticFileQueryParam param) throws IOException {
		File rootFile = root.toFile();

		Predicate<File> predicate = !param.needQuery() ? p -> true : p -> {
			String name = p.getName();
			return matchParam(param, name);
		};

		File[] fileArray = rootFile.listFiles();

		if (fileArray == null) {
			return new PageResult<>(param, 0, new ArrayList<>());
		}

		fileArray = Arrays.stream(fileArray).filter(predicate).toArray(File[]::new);

		Arrays.sort(fileArray, Comparator.comparingLong(File::lastModified).reversed());

		int total = fileArray.length;
		if (param.getOffset() >= total) {
			return new PageResult<>(param, total, new ArrayList<>());
		}

		int to = Math.min(total, param.getOffset() + param.getPageSize());

		List<StaticFile> files = new ArrayList<>();
		for (int i = param.getOffset(); i < to; i++) {
			files.add(toStaticFile(fileArray[i].toPath()));
		}

		return new PageResult<>(param, total, files);
	}

	/**
	 * 查询文件夹下所有的文件
	 * 
	 * @param root
	 *            根目录
	 * @param param
	 *            查询参数
	 * @return
	 * @throws IOException
	 */
	protected PageResult<StaticFile> doWalkSearch(Path root, StaticFileQueryParam param) throws IOException {
		Predicate<Path> predicate = !param.needQuery() ? p -> true
				: p -> matchParam(param, Objects.toString(p.getFileName(), null));
		Path[] paths = Files.walk(root).filter(predicate).toArray(Path[]::new);

		Arrays.sort(paths, Comparator.comparingLong(FileUtils::getLastModifiedTime).reversed());

		int total = paths.length;
		if (param.getOffset() >= total) {
			return new PageResult<>(param, total, new ArrayList<>());
		}

		int to = Math.min(total, param.getOffset() + param.getPageSize());

		List<StaticFile> files = new ArrayList<>();
		for (int i = param.getOffset(); i < to; i++) {
			files.add(toStaticFile(paths[i]));
		}
		return new PageResult<>(param, total, files);
	}

	private boolean matchParam(StaticFileQueryParam param, String name) {
		String ext = FileUtils.getFileExtension(name);
		if (!CollectionUtils.isEmpty(param.getExtensions())
				&& param.getExtensions().stream().noneMatch(ex -> ex.equalsIgnoreCase(ext))) {
			return false;
		}
		String mName = param.getName();
		return Validators.isEmptyOrNull(mName, true) || name.contains(mName);
	}

	private StaticFile toStaticFile(Path path) {
		StaticFile lf = new StaticFile();
		lf.setDir(Files.isDirectory(path));
		lf.setName(Objects.toString(path.getFileName()));
		if (FileUtils.isRegularFile(path)) {
			lf.setExt(FileUtils.getFileExtension(lf.getName()));
		}
		// 这里转化为File然后获取大小，Files.size会去检查文件是否存在，但无法保证文件一定存在?
		lf.setSize(path.toFile().length());
		lf.setPath(FileUtils.cleanPath(root.relativize(path).toString()));

		if (!lf.isDir()) {
			lf.setUrl(getUrl(lf.getPath()));
		}
		lf.setEditable(isEditable(path));
		return lf;
	}

	private Path resolve(Path root, String path) {
		Path resolve;
		if (Validators.isEmptyOrNull(path, true)) {
			resolve = root;
		} else {
			String cpath = FileUtils.cleanPath(path);
			resolve = Validators.isEmptyOrNull(cpath, true) ? root : root.resolve(cpath);
		}
		return resolve;
	}

	/**
	 * 移动|重命名文件
	 * 
	 * @param path
	 *            旧路径
	 * @param newPath
	 *            新路径
	 * @throws LogicException
	 */

	public void move(String path, String newPath) throws LogicException {
		lock.writeLock().lock();
		try {
			Path p = byPath(validatePath(path));
			if (p == this.root) {
				throw new LogicException("staticFile.move.root", "根目录无法被移动");
			}

			String _newPath = newPath;
			// 如果移动文件，不应该改变文件后缀
			if (FileUtils.isRegularFile(p)) {
				String ext = FileUtils.getFileExtension(p);
				if (!ext.isEmpty()) {
					_newPath += ("." + ext);
				}
			}

			Path dest = resolve(this.root, validatePath(_newPath));

			if (!FileUtils.isSub(dest, this.root)) {
				throw new LogicException("staticFile.move.dest.notInRoot", "文件移动目标位置不在根目录内");
			}

			if (p.equals(dest)) {
				return;
			}

			if (p.equals(dest.resolve(p.getFileName()))) {
				return;
			}

			if (Files.exists(dest)) {
				Path relative = this.root.relativize(dest);
				throw new LogicException("staticFile.move.dest.exists", "目标位置已经存在文件:" + relative, relative);
			}

			if (FileUtils.isSub(dest, p)) {
				Path relativeDest = this.root.relativize(dest);
				Path relativeP = this.root.relativize(p);
				throw new LogicException("staticFile.move.parentPath",
						"目标文件:" + relativeDest + "不能是原文件:" + relativeP + "的子文件", relativeDest, relativeP);
			}

			List<Path> rollBacks = new ArrayList<>();

			try {

				rollBacks.addAll(createDirectories(dest.getParent()));

				Files.move(p, dest, StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception e) {

				delete(rollBacks);

				if (e instanceof LogicException) {
					throw (LogicException) e;
				}

				if (e instanceof FileAlreadyExistsException) {
					Path relative = this.root.relativize(dest);
					throw new LogicException("staticFile.move.file.exists", "位置:" + relative + "已经存在文件", relative);
				}

				throw new SystemException(e.getMessage(), e);
			}

		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 拷贝文件
	 * 
	 * @param path
	 *            被拷贝文件路径
	 * @param destPath
	 *            目标文件夹
	 * @throws LogicException
	 */
	public void copy(String path, String destPath) throws LogicException {
		lock.writeLock().lock();
		try {
			Path p = byPath(validatePath(path));
			if (p == this.root) {
				throw new LogicException("staticFile.copy.notRoot", "根目录无法被拷贝");
			}

			Path dest = resolve(this.root, validatePath(destPath));
			if (!FileUtils.isSub(dest, this.root)) {
				throw new LogicException("staticFile.copy.notInRoot", "目标位置不在根目录内");
			}

			if (p.equals(dest)) {
				throw new LogicException("staticFile.copy.samePath", "目标文件不能和原文件相同");
			}

			if (p.equals(dest.resolve(p.getFileName()))) {
				Path relative = this.root.relativize(p);
				throw new LogicException("staticFile.copy.file.exists", "文件" + relative + "已经存在", relative);
			}

			if (FileUtils.isSub(dest, p)) {
				Path relativeDest = this.root.relativize(dest);
				Path relativeP = this.root.relativize(p);
				throw new LogicException("staticFile.copy.parentPath",
						"目标文件:" + relativeDest + "不能是原文件:" + relativeP + "的子文件", relativeDest, relativeP);
			}
			try {
				doCopy(p, dest);
			} catch (RuntimeLogicException e) {
				throw e.getLogicException();
			}

		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 获取<b>可编辑</b>文件内容
	 * 
	 * @param path
	 * @return
	 * @throws LogicException
	 */
	public FileContent getEditableFile(String path) throws LogicException {
		String validPath = validatePath(path);
		lock.readLock().lock();
		try {
			Path file = byPath(validPath);
			if (!FileUtils.isRegularFile(file)) {
				throw new LogicException("staticFile.edit.notFile", "只有文件才能被编辑");
			}
			if (!isEditable(file)) {
				throw new LogicException("staticFile.edit.unable", "该文件不能被编辑");
			}
			FileContent fc = new FileContent();
			try {
				fc.setContent(new String(Files.readAllBytes(file), Constants.CHARSET));
			} catch (IOException e) {
				if (FileUtils.exists(file)) {
					logger.error(e.getMessage(), e);
				}
				throw new LogicException("staticFile.edit.getContentFail", "获取文件内容失败");
			}
			fc.setExt(FileUtils.getFileExtension(path).toLowerCase());
			fc.setPath(validPath);
			return fc;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 编辑文件
	 * 
	 * @param path
	 * @param content
	 * @throws LogicException
	 */
	public void editFile(String path, String content) throws LogicException {
		String validPath = validatePath(path);
		lock.writeLock().lock();
		try {
			Path file = byPath(validPath);
			if (!isEditable(file)) {
				throw new LogicException("staticFile.edit.unable", "该文件不能被编辑");
			}
			try {
				Files.write(file, content.getBytes(Constants.CHARSET));
			} catch (Exception e) {
				if (FileUtils.exists(file)) {
					logger.error(e.getMessage(), e);
				}
				throw new LogicException("staticFile.edit.writeError", "文件写入失败");
			}

		} finally {
			lock.writeLock().unlock();
		}
	}

	protected void doCopy(Path source, Path dest) throws LogicException {

		List<Path> rollBacks = Collections.synchronizedList(createDirectories(dest));

		if (FileUtils.isRegularFile(source)) {
			Path copied = dest.resolve(source.getFileName());
			boolean exists = FileUtils.exists(copied);
			try {
				Files.copy(source, copied);
			} catch (Exception e) {

				if (!exists) {
					FileUtils.deleteQuietly(copied);
				}

				delete(rollBacks);

				if (e instanceof FileAlreadyExistsException) {
					Path relative = this.root.relativize(copied);
					throw new LogicException("staticFile.copy.file.exists", "位置:" + relative + "已经存在文件", relative);
				}

				throw new SystemException(e.getMessage(), e);
			}
		}

		if (FileUtils.isDirectory(source)) {

			Path root = dest.resolve(source.getFileName());

			walk(source).filter(FileUtils::isRegularFile).parallel().forEach(path -> {

				Path target = root.resolve(source.relativize(path));

				boolean exists = FileUtils.exists(target);

				try {
					rollBacks.addAll(createDirectories(target.getParent()));
					Files.copy(path, target);
					rollBacks.add(target);

				} catch (Exception e) {

					if (!exists) {
						FileUtils.deleteQuietly(target);
					}

					delete(rollBacks);

					if (e instanceof LogicException) {
						throw new RuntimeLogicException((LogicException) e);
					}

					if (e instanceof FileAlreadyExistsException) {
						Path relative = this.root.relativize(dest);
						throw new RuntimeLogicException(
								new Message("staticFile.copy.file.exists", "位置:" + relative + "已经存在文件", relative));
					}

					throw new SystemException(e.getMessage(), e);
				}
			});
		}
	}

	protected void delete(List<Path> paths) {
		paths.forEach(FileUtils::deleteQuietly);
	}

	/**
	 * 统计文件|文件夹 数目以及 文件总大小
	 * 
	 * @return
	 */
	public StaticFileStatistics queryFileStatistics() {
		lock.readLock().lock();
		try {
			long total = Files.walk(root).filter(FileUtils::isRegularFile).parallel().mapToLong(FileUtils::getSize)
					.sum();
			int dirCount = (int) Files.walk(root).filter(FileUtils::isDirectory).parallel().count();
			int fileCount = (int) Files.walk(root).filter(FileUtils::isRegularFile).parallel().count();

			return new StaticFileStatistics(dirCount, fileCount, total);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
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
	 * @param path
	 *            文件路径
	 * @throws LogicException
	 */
	public void delete(String path) throws LogicException {
		lock.writeLock().lock();
		try {
			Path toDelete = byPath(validatePath(path));
			if (toDelete == this.root) {
				throw new LogicException("staticFile.delete.root", "根目录无法删除");
			}
			FileUtils.deleteQuietly(toDelete);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 创建文件夹
	 * 
	 * @param path
	 * @throws LogicException
	 */
	public void createDirectorys(String path) throws LogicException {
		lock.writeLock().lock();
		try {

			Path dir = root.resolve(validatePath(path));

			if (!FileUtils.isSub(dir, root)) {
				throw new LogicException("staticFile.createDir.notInRoot", "文件不在根目录内");
			}

			createDirectories(dir);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 创建文件
	 * 
	 * @param path
	 */
	public void createFile(String path) throws LogicException {
		lock.writeLock().lock();
		try {

			Path file = root.resolve(validatePath(path));

			if (!FileUtils.isSub(file, root)) {
				throw new LogicException("staticFile.createFile.notInRoot", "文件不在根目录内");
			}

			if (FileUtils.exists(file)) {
				throw new LogicException("staticFile.createFile.exists", "文件已经存在");
			}

			List<Path> paths = createDirectories(file.getParent());
			try {
				Files.createFile(file);
			} catch (FileAlreadyExistsException e) {
				delete(paths);
				throw new LogicException("staticFile.createFile.exists", "文件已经存在");
			} catch (Exception e) {
				delete(paths);
				logger.error(e.getMessage(), e);
				throw new LogicException("staticFile.createFile.fail", "文件创建失败");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 校验路径是否正确
	 * 
	 * @param path
	 *            如果路径正确，返回格式化过的路径
	 * @return
	 * @throws LogicException
	 */
	private String validatePath(String path) throws LogicException {
		if (!Validators.isEmptyOrNull(path, true)) {
			path = FileUtils.cleanPath(path);
			if (!Validators.isEmptyOrNull(path, true)) {

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

	private void validSlashPath(String path) throws LogicException {
		if (!FileUtils.maybeValidateFilename(path)) {
			throw new LogicException("file.name.valid", "文件名:" + path + "非法", path);
		}
		if (path.length() > MAX_NAME_LENGTH) {
			throw new LogicException("file.name.toolong", "文件名:" + path + "不能超过" + MAX_NAME_LENGTH + "个字符", path,
					MAX_NAME_LENGTH);
		}
	}

	/**
	 * 找出两个path之间的路径
	 * <p>
	 * 例如，betweenPaths(Paths.get("c:/123"),Paths.get("c:/123/456/789/xxx"))，
	 * 返回["456","789"]
	 * </p>
	 * 
	 * @param root
	 * @param dir
	 * @return
	 */
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
		throw new SystemException("无法找出两个path之间的路径");
	}

	/**
	 * 获取某个文件的web访问路径
	 * 
	 * @param path
	 * @return
	 */
	protected String getUrl(String path) {
		return urlHelper.getUrl() + "/" + prefix + "/" + FileUtils.cleanPath(path);
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.setContentNegotiationManager(contentNegotiationManager);

		super.afterPropertiesSet();
		setLocations(List.of(new PathResource(root)));
	}

	@EventListener(ContextRefreshedEvent.class)
	void start(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			return;
		}
		WebApplicationContext ctx = (WebApplicationContext) event.getApplicationContext();
		StaticResourceUrlHandlerMapping urlMapping = ctx.getBean(StaticResourceUrlHandlerMapping.class);
		urlMapping.registerResourceHttpRequestHandlerMapping("/" + prefix + "/**", this);
	}

	/**
	 * 将文件|文件夹打包成ZIP文件
	 * 
	 * @param path
	 * @param zipPath
	 */
	public void packZip(String path, String zipPath) throws LogicException {
		lock.writeLock().lock();

		try {
			Path p = byPath(validatePath(path));

			if (p == this.root) {
				throw new LogicException("staticFile.zip.root", "根目录不能打包成zip");
			}

			Path zip = resolve(this.root, validatePath(zipPath + "." + ZIP.toLowerCase()));

			if (!FileUtils.isSub(zip, this.root)) {
				throw new LogicException("staticFile.zip.notInRoot", "目标位置不在根目录内");
			}

			List<Path> rollBacks = createDirectories(zip.getParent());

			try {
				rollBacks.add(Files.createFile(zip));
			} catch (Exception e) {

				delete(rollBacks);

				if (e instanceof FileAlreadyExistsException) {
					Path relative = this.root.relativize(zip);
					throw new LogicException("staticFile.zip.file.exists", "位置" + relative + "已经存在文件");
				}

				throw new SystemException(e.getMessage(), e);
			}

			try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(zip));
					ZipOutputStream zs = new ZipOutputStream(bos)) {

				if (FileUtils.isRegularFile(p)) {
					ZipEntry zipEntry = new ZipEntry(p.getFileName().toString());
					putInEntry(zs, zipEntry, p);
				}

				if (FileUtils.isDirectory(p)) {
					Files.walk(p).filter(_p -> !FileUtils.isDirectory(_p)).forEach(_p -> {
						ZipEntry zipEntry = new ZipEntry(p.relativize(_p).toString());
						try {
							putInEntry(zs, zipEntry, _p);
						} catch (IOException e) {
							throw new SystemException(e.getMessage(), e);
						}
					});
				}
			} catch (Exception e) {
				delete(rollBacks);

				if (e instanceof SystemException) {
					throw (SystemException) e;
				}

				throw new SystemException(e.getMessage(), e);
			}
		} finally {
			lock.writeLock().unlock();
		}

	}

	private void putInEntry(ZipOutputStream zs, ZipEntry entry, Path _p) throws IOException {
		zs.putNextEntry(entry);
		Files.copy(_p, zs);
		zs.closeEntry();
	}

	protected final Stream<Path> walk(Path path) {
		try {
			return Files.walk(path);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	protected boolean isEditable(Path file) {
		String ext = FileUtils.getFileExtension(file).toLowerCase();
		for (String editableExt : editableExts) {
			if (ext.equals(editableExt)) {
				return true;
			}
		}
		return false;
	}

}
