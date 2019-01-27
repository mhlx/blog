package me.qyh.blog.file.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.ResourceNotFoundException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.impl.Sync;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.file.dao.BlogFileDao;
import me.qyh.blog.file.dao.CommonFileDao;
import me.qyh.blog.file.dao.FileDeleteDao;
import me.qyh.blog.file.entity.BlogFile;
import me.qyh.blog.file.entity.BlogFile.BlogFileType;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.entity.FileDelete;
import me.qyh.blog.file.service.FileService;
import me.qyh.blog.file.store.FileManager;
import me.qyh.blog.file.store.FileStore;
import me.qyh.blog.file.store.ImageHelper;
import me.qyh.blog.file.vo.BlogFileCount;
import me.qyh.blog.file.vo.BlogFilePageResult;
import me.qyh.blog.file.vo.BlogFileProperties;
import me.qyh.blog.file.vo.BlogFileQueryParam;
import me.qyh.blog.file.vo.BlogFileUpload;
import me.qyh.blog.file.vo.ExpandedCommonFile;
import me.qyh.blog.file.vo.FileCountBean;
import me.qyh.blog.file.vo.FileStatistics;
import me.qyh.blog.file.vo.FileStoreBean;
import me.qyh.blog.file.vo.UploadedFile;

/**
 * {@link http://mikehillyer.com/articles/managing-hierarchical-data-in-mysql}
 * 
 * @author Administrator
 *
 */
@Service("fileService")
public class FileServiceImpl implements FileService, InitializingBean {

	@Autowired
	private FileManager fileManager;
	@Autowired
	private BlogFileDao blogFileDao;
	@Autowired
	private FileDeleteDao fileDeleteDao;
	@Autowired
	private CommonFileDao commonFileDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

	private static final Message PARENT_NOT_EXISTS = new Message("file.parent.notExists", "父目录不存在");
	private static final Message NOT_EXISTS = new Message("file.notExists", "文件不存在");

	private static final long MAX_MODIFY_TIME = 1800000;

	private static final int MAX_PATH_LENGTH = 255;

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public List<UploadedFile> upload(BlogFileUpload upload) throws LogicException {
		BlogFile parent;
		if (upload.getParent() != null) {
			parent = blogFileDao.selectById(upload.getParent())
					.orElseThrow(() -> new LogicException(PARENT_NOT_EXISTS));
		} else {
			parent = blogFileDao.selectRoot();
		}

		/**
		 * 5.7
		 */
		if (!parent.isRoot() && !parent.isDir()) {
			throw new LogicException("file.parent.mustDir", "父目录必须是一个文件夹");
		}

		String folderKey = getFilePath(parent);
		deleteImmediatelyIfNeed(folderKey);
		Integer storeId = upload.getStore();
		if (storeId == null) {
			throw new LogicException("file.store.notexists", "文件存储器不存在");
		}
		List<UploadedFile> uploadedFiles = new ArrayList<>();
		for (MultipartFile file : upload.getFiles()) {
			String originalFilename = file.getOriginalFilename();
			try {
				FileStore store = getFileStore(storeId, file);
				UploadedFile uf = storeMultipartFile(file, parent, folderKey, store);
				uploadedFiles.add(uf);
			} catch (LogicException e) {
				uploadedFiles.add(new UploadedFile(originalFilename, e.getLogicMessage()));
			}
		}
		return uploadedFiles;
	}

	private FileStore getFileStore(Integer storeId, MultipartFile file) throws LogicException {
		if (storeId.intValue() == -1) {
			return fileManager.getAllStores().stream().filter(store -> !store.readOnly() && store.canStore(file))
					.sorted((s1, s2) -> -Integer.compare(s1.getOrder(file), s2.getOrder(file))).findFirst()
					.orElseThrow(() -> new LogicException("file.store.noMatchable", "没有合适的文件存储器"));
		}
		FileStore store = fileManager.getFileStore(storeId)
				.orElseThrow(() -> new LogicException("file.store.notexists", "文件存储器不存在"));
		if (store.readOnly()) {
			throw new LogicException("file.store.readonly", "只读存储器无法存储文件");
		}
		if (!store.canStore(file)) {
			throw new LogicException("file.store.unsupport", "存储器不支持存储这个文件");
		}
		return store;
	}

	private UploadedFile storeMultipartFile(MultipartFile mf, BlogFile parent, String folderKey, FileStore store)
			throws LogicException {

		MultipartFile file = store.preHandler(mf);

		String originalFilename = file.getOriginalFilename();

		validateSlashPath(originalFilename);

		String ext = FileUtils.getFileExtension(originalFilename);
		String name = FileUtils.getNameWithoutExtension(originalFilename);
		String fullname = ext.isEmpty() ? name : name + "." + ext.toLowerCase();

		if (blogFileDao.selectByParentAndPath(parent, fullname).isPresent()) {
			throw new LogicException("file.path.exists", "文件已经存在");
		}

		String key = folderKey.isEmpty() ? fullname : (folderKey + "/" + fullname);
		deleteImmediatelyIfNeed(key);
		CommonFile cf = store.store(key, file);

		// 如果不是被支持的图片格式
		if (ImageHelper.isSystemAllowedImage(ext) && !ImageHelper.isSystemAllowedImage(cf.getExtension())) {
			store.delete(key);
			throw new LogicException("file.unsupportformat", "不支持" + cf.getExtension() + "格式的文件", cf.getExtension());
		}
		try {
			commonFileDao.insert(cf);
			BlogFile blogFile = new BlogFile();
			blogFile.setCf(cf);
			blogFile.setPath(fullname);
			blogFile.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
			blogFile.setLft(parent.getLft() + 1);
			blogFile.setRgt(parent.getLft() + 2);
			blogFile.setParent(parent);
			blogFile.setType(BlogFileType.FILE);

			blogFileDao.updateWhenAddChild(parent);
			blogFileDao.insert(blogFile);
			return new UploadedFile(originalFilename, cf.getSize(), store.getThumbnailUrl(key).orElse(null),
					store.getUrl(key));
		} catch (RuntimeException | Error e) {
			store.delete(key);
			LOGGER.error(e.getMessage(), e);
			throw new LogicException(Constants.SYSTEM_ERROR);
		}
	}

	private void deleteImmediatelyIfNeed(String path) throws LogicException {
		String clean = FileUtils.cleanPath(path);
		if (clean.isEmpty()) {
			return;
		}
		String rootKey = clean.split("/")[0];
		List<FileDelete> children = fileDeleteDao.selectChildren(rootKey);
		if (children.isEmpty()) {
			return;
		}
		for (FileDelete child : children) {
			deleteFile(child);
		}
	}

	private void deleteFile(FileDelete fd) throws LogicException {
		if (fd.getType().equals(BlogFileType.DIRECTORY)) {
			deleteDirectory(fd);
		} else {
			deleteOne(fd);
		}
	}

	private void deleteDirectory(FileDelete fd) throws LogicException {
		String key = fd.getKey();
		for (FileStore store : fileManager.getAllStores()) {
			if (!store.deleteBatch(key)) {
				throw new LogicException("file.batchDelete.fail", "存储器" + store.id() + "无法删除目录" + key + "下的文件",
						store.id(), key);
			}
		}
		fileDeleteDao.deleteById(fd.getId());
	}

	private void deleteOne(FileDelete fd) throws LogicException {
		String key = fd.getKey();
		Optional<FileStore> optionalFileStore = fileManager.getFileStore(fd.getStore());
		if (optionalFileStore.isPresent()) {
			FileStore fs = optionalFileStore.get();
			if (!fs.delete(key)) {
				throw new LogicException("file.delete.fail", "文件删除失败，无法删除存储器" + fs.id() + "下" + key + "对应的文件", fs.id(),
						key);
			}
		} else {
			LOGGER.warn("无法找到id为" + fd.getStore() + "的存储器");
		}
		fileDeleteDao.deleteById(fd.getId());
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public BlogFile createFolder(BlogFile toCreate) throws LogicException {

		BlogFile parent = toCreate.getParent();
		if (parent != null) {
			parent = blogFileDao.selectById(parent.getId()).orElseThrow(() -> new LogicException(PARENT_NOT_EXISTS));
			if (!parent.isDir()) {
				throw new LogicException("file.parent.mustDir", "父目录必须是一个文件夹");
			}
		} else {
			// 查询根节点
			parent = blogFileDao.selectRoot();
		}

		return createFolder(parent, toCreate.getPath());
	}

	private BlogFile createFolder(String path) throws LogicException {

		String cleanedPath = FileUtils.cleanPath(path);
		if (cleanedPath.isEmpty()) {
			return blogFileDao.selectRoot();
		} else {
			if (cleanedPath.indexOf('/') == -1) {
				return createFolder(blogFileDao.selectRoot(), cleanedPath);
			} else {
				BlogFile parent = blogFileDao.selectRoot();
				for (String _path : cleanedPath.split("/")) {
					parent = createFolder(parent, _path);
				}
				return parent;
			}
		}
	}

	private BlogFile createFolder(BlogFile parent, String folder) throws LogicException {

		validateSlashPath(folder);

		Optional<BlogFile> op = blogFileDao.selectByParentAndPath(parent, folder);
		if (op.isPresent()) {
			BlogFile checked = op.get();
			if (checked.isDir()) {
				return checked;
			} else {
				throw new LogicException("file.path.exists", "文件已经存在");
			}
		}
		BlogFile bf = new BlogFile();
		bf.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
		bf.setLft(parent.getLft() + 1);
		bf.setRgt(parent.getLft() + 2);
		bf.setParent(parent);
		bf.setPath(folder);
		bf.setType(BlogFileType.DIRECTORY);
		blogFileDao.updateWhenAddChild(parent);
		blogFileDao.insert(bf);
		return bf;
	}

	@Override
	@Transactional(readOnly = true)
	public BlogFilePageResult queryBlogFiles(BlogFileQueryParam param) throws LogicException {
		List<BlogFile> paths = null;
		if (param.getParent() != null) {
			BlogFile parent = blogFileDao.selectById(param.getParent())
					.orElseThrow(() -> new LogicException(PARENT_NOT_EXISTS));
			if (!parent.isDir()) {
				throw new LogicException("file.parent.mustDir", "父目录必须是一个文件夹");
			}
			paths = blogFileDao.selectPath(parent);
			param.setParentFile(parent);
		} else {
			param.setParentFile(blogFileDao.selectRoot());
		}

		List<BlogFile> datas = blogFileDao.selectPage(param);
		for (BlogFile file : datas) {
			setExpandedCommonFile(file);
		}

		PageResult<BlogFile> page;
		if (!param.isIgnorePaging()) {
			int count = blogFileDao.selectCount(param);
			page = new PageResult<>(param, count, datas);
		} else {
			page = new PageResult<>(param, datas.size(), datas);
		}

		BlogFilePageResult result = new BlogFilePageResult();
		result.setPage(page);
		if (paths != null) {
			paths.remove(0);
			result.setPaths(paths);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public BlogFileProperties getBlogFileProperties(Integer id) throws LogicException {
		BlogFile file = blogFileDao.selectById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_EXISTS));
		Map<String, Object> base = new HashMap<>();
		base.put("type", file.getType());
		if (file.isDir()) {
			base.put("counts", blogFileDao.selectSubBlogFileCount(file));
			base.put("size", blogFileDao.selectSubBlogFileSize(file));
			return new BlogFileProperties(base);
		}
		CommonFile cf = file.getCf();
		if (cf != null) {
			String key = getFilePath(file);
			FileStore fs = getFileStore(cf);
			base.put("url", fs.getUrl(key));
			base.put("thumbUrl", fs.getThumbnailUrl(key));
			base.put("size", cf.getSize());
			return new BlogFileProperties(base, fs.getProperties(key));
		}
		return new BlogFileProperties(base);
	}

	@Override
	public List<FileStore> allStorableStores() {
		return fileManager.getAllStores().stream().filter(Predicate.not(FileStore::readOnly))
				.collect(Collectors.toList());
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void copy(Integer sourceId, String folderPath) throws LogicException {
		BlogFile source = blogFileDao.selectById(sourceId).orElseThrow(() -> new ResourceNotFoundException(NOT_EXISTS));
		if (!source.isFile()) {
			throw new LogicException("file.copy.onlyFile", "只有文件才能被拷贝");
		}

		deleteImmediatelyIfNeed(folderPath);

		String oldPath = getFilePath(source);
		BlogFile parent = createFolder(folderPath);
		// 路径上存在文件
		if (blogFileDao.selectByParentAndPath(parent, source.getPath()).isPresent()) {
			throw new LogicException("file.path.exists", "文件已经存在");
		}

		CommonFile copyCf = new CommonFile(source.getCf());
		BlogFile copyBf = new BlogFile();
		copyBf.setCf(copyCf);
		copyBf.setCreateDate(Timestamp.valueOf(Times.now()));
		copyBf.setCf(copyCf);
		copyBf.setPath(source.getPath());
		copyBf.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
		copyBf.setLft(parent.getLft() + 1);
		copyBf.setRgt(parent.getLft() + 2);
		copyBf.setParent(parent);
		copyBf.setType(BlogFileType.FILE);

		blogFileDao.updateWhenAddChild(parent);
		blogFileDao.insert(copyBf);

		FileStore fs = getFileStore(source.getCf());
		String destPath = folderPath + "/" + source.getPath();
		if (!fs.copy(oldPath, destPath)) {
			throw new LogicException("file.copy.fail", "文件拷贝失败");
		}
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void move(Integer sourceId, String newPath) throws LogicException {
		BlogFile db = blogFileDao.selectById(sourceId).orElseThrow(() -> new ResourceNotFoundException(NOT_EXISTS));
		if (!db.isFile()) {
			throw new LogicException("file.move.onlyFile", "只有文件才能被移动");
		}
		String oldPath = FileUtils.cleanPath(getFilePath(db));
		String path = FileUtils.cleanPath(newPath);

		// 先删除节点
		blogFileDao.delete(db);
		blogFileDao.updateWhenDelete(db);

		// 如果目标文件夹待删除，立即删除
		deleteImmediatelyIfNeed(path);

		// 创建文件夹，如果不存在
		BlogFile parent = createFolder(newPath);
		// 路径上存在文件
		if (blogFileDao.selectByParentAndPath(parent, db.getPath()).isPresent()) {
			throw new LogicException("file.path.exists", "文件已经存在");
		}

		// 更新父节点左右值
		blogFileDao.updateWhenAddChild(parent);

		BlogFile bf = new BlogFile();
		bf.setCf(db.getCf());
		bf.setCreateDate(db.getCreateDate());
		bf.setLft(parent.getLft() + 1);
		bf.setRgt(parent.getLft() + 2);
		bf.setParent(parent);
		bf.setPath(db.getPath());
		bf.setType(BlogFileType.FILE);

		// 插入新节点
		blogFileDao.insert(bf);

		// 移动实际文件
		FileStore fs = getFileStore(db.getCf());
		if (!fs.move(oldPath, path + "/" + db.getPath())) {
			throw new LogicException("file.move.fail", "文件移动失败");
		}
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void rename(Integer id, String newName) throws LogicException {
		BlogFile db = blogFileDao.selectById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_EXISTS));
		if (!db.isFile()) {
			throw new LogicException("file.rename.onlyFile", "只有文件才能被重命名");
		}
		String oldName = FileUtils.getNameWithoutExtension(db.getPath());
		if (newName.equals(oldName)) {
			return;
		}
		String ext = FileUtils.getFileExtension(db.getPath());
		String name = newName;
		if (!Validators.isEmptyOrNull(ext, true)) {
			name = name + "." + ext;
		}

		validateSlashPath(name);

		BlogFile parent;
		if (db.getParent() == null) {
			parent = blogFileDao.selectRoot();
		} else {
			parent = blogFileDao.selectById(db.getParent().getId()).orElse(null);
		}

		BlogFile checked = blogFileDao.selectByParentAndPath(parent, name).orElse(null);
		// 路径上存在文件
		if (checked != null && !Objects.equals(db.getId(), checked.getId())) {
			throw new LogicException("file.path.exists", "文件已经存在");
		}

		String oldPath = FileUtils.cleanPath(getFilePath(db));
		String newPath;
		if (oldPath.indexOf('/') == -1) {
			newPath = name;
		} else {
			newPath = oldPath.substring(0, oldPath.lastIndexOf('/')) + "/" + name;
		}

		db.setPath(name);
		blogFileDao.update(db);

		// 移动实际文件
		FileStore fs = getFileStore(db.getCf());
		if (!fs.move(oldPath, newPath)) {
			throw new LogicException("file.move.fail", "文件移动失败");
		}

	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void delete(Integer id) throws LogicException {
		BlogFile db = blogFileDao.selectById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_EXISTS));
		if (db.getParent() == null) {
			throw new LogicException("file.root.canNotDelete", "根节点不能删除");
		}
		String key = getFilePath(db);
		// 删除文件记录
		blogFileDao.delete(db);
		blogFileDao.deleteCommonFile(db);
		// 更新受影响节点的左右值
		blogFileDao.updateWhenDelete(db);

		FileDelete fd = new FileDelete();
		if (db.isFile()) {
			fileManager.getFileStore(db.getCf().getStore()).ifPresent(store -> fd.setStore(store.id()));
		} else {
			fileDeleteDao.deleteChildren(key);
		}
		fd.setKey(key);
		fd.setType(db.getType());
		fileDeleteDao.insert(fd);
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void clear() {
		// 删除超过一段时间的临时文件
		FileUtils.clearAppTemp(this::overMaxModifyTime);
		List<FileDelete> all = fileDeleteDao.selectAll();
		for (FileDelete fd : all) {
			try {
				deleteFile(fd);
			} catch (LogicException e) {
			}
		}
		blogFileDao.deleteUnassociateCommonFile();

	}

	private boolean overMaxModifyTime(Path path) {
		try {
			long lastModifyMill = Files.getLastModifiedTime(path).toMillis();
			return System.currentTimeMillis() - lastModifyMill > MAX_MODIFY_TIME;
		} catch (IOException e) {
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * path层次过深会影响效率
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResult<BlogFile> queryFiles(String path, BlogFileQueryParam param) {
		BlogFile parent = blogFileDao.selectRoot();
		String cleanedPath =
				// since 5.7
				path == null ? "" : FileUtils.cleanPath(path.strip());
		if (cleanedPath.isEmpty()) {
			param.setParentFile(parent);
		} else {
			if (cleanedPath.indexOf('/') == -1) {
				parent = blogFileDao.selectByParentAndPath(parent, cleanedPath).orElse(null);
			} else {
				for (String _path : cleanedPath.split("/")) {
					parent = blogFileDao.selectByParentAndPath(parent, _path).orElse(null);
					if (parent == null) {
						break;
					}
				}
			}

			if (parent == null || parent.isFile()) {
				return new PageResult<>(param, 0, new ArrayList<>());
			}

			param.setParentFile(parent);
		}
		// param.setType(BlogFileType.FILE);
		param.setQuerySubDir(true);

		List<BlogFile> datas = blogFileDao.selectPage(param);

		for (BlogFile file : datas) {
			setExpandedCommonFile(file);
		}

		if (!param.isIgnorePaging()) {
			int count = blogFileDao.selectCount(param);
			return new PageResult<>(param, count, datas);
		} else {
			return new PageResult<>(param, datas.size(), datas);
		}

	}

	@Override
	@Transactional(readOnly = true)
	public FileStatistics queryFileStatistics() {
		FileStatistics fileStatistics = new FileStatistics();
		BlogFile root = blogFileDao.selectRoot();
		fileStatistics.setTypeCountMap(blogFileDao.selectSubBlogFileCount(root).stream()
				.collect(Collectors.toMap(BlogFileCount::getType, BlogFileCount::getCount)));
		fileStatistics.setStoreCountMap(blogFileDao.selectFileCount().stream()
				.collect(Collectors.toMap(this::wrap, FileCountBean::getFileCount)));
		return fileStatistics;
	}

	private FileStoreBean wrap(FileCountBean fcb) {
		int fileStore = fcb.getFileStore();
		return fileManager.getFileStore(fileStore).map(FileStoreBean::new)
				.orElseThrow(() -> new SystemException("文件存储器:" + fileStore + "不存在"));
	}

	private void setExpandedCommonFile(BlogFile bf) {
		CommonFile cf = bf.getCf();
		if (cf != null && bf.isFile()) {
			String key = getFilePath(bf);
			FileStore fs = getFileStore(cf);
			ExpandedCommonFile pcf = new ExpandedCommonFile(cf);
			pcf.setThumbnailUrl(fs.getThumbnailUrl(key).orElse(null));
			pcf.setUrl(fs.getUrl(key));
			bf.setCf(pcf);
		}
	}

	private FileStore getFileStore(CommonFile cf) {
		return fileManager.getFileStore(cf.getStore())
				.orElseThrow(() -> new SystemException("没有找到ID为:" + cf.getStore() + "的存储器"));
	}

	private String getFilePath(BlogFile bf) {
		List<BlogFile> files = blogFileDao.selectPath(bf);
		return files.stream().map(BlogFile::getPath).filter(Predicate.not(String::isEmpty))
				.collect(Collectors.joining("/"));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 找根目录
		if (blogFileDao.selectRoot() == null) {
			LOGGER.debug("没有找到任何根目录，将创建一个根目录");
			BlogFile root = new BlogFile();
			root.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
			root.setLft(1);
			root.setPath("");
			root.setRgt(2);
			root.setType(BlogFileType.DIRECTORY);
			blogFileDao.insert(root);
		}
	}

	private void validateSlashPath(String path) throws LogicException {
		if (!FileUtils.maybeValidateFilename(path)) {
			throw new LogicException("file.name.valid", "文件名" + path + "无效", path);
		}

		if (path.length() > MAX_PATH_LENGTH) {
			throw new LogicException("file.name.toolong", "文件名:" + path + "不能超过" + MAX_PATH_LENGTH + "个字符", path,
					MAX_PATH_LENGTH);
		}
	}

}
