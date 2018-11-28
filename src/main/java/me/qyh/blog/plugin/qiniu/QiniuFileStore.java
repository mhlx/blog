package me.qyh.blog.plugin.qiniu;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.BucketManager.Batch;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Jsons.ExpressionExecutor;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.FileStore;
import me.qyh.blog.file.store.ImageHelper;
import me.qyh.blog.file.store.ImageHelper.ImageInfo;
import me.qyh.blog.file.store.Resize;
import me.qyh.blog.file.store.ThumbnailUrl;

/**
 * 
 * @author Administrator
 *
 */
public class QiniuFileStore implements FileStore {

	protected static final Logger LOGGER = LoggerFactory.getLogger(QiniuFileStore.class);

	private final int id;
	private final String name;

	protected Resize smallResize;
	protected Resize middleResize;
	protected Resize largeResize;

	private boolean readOnly;

	private String urlPrefix;// 外链域名
	private Character styleSplitChar;// 样式分隔符
	private boolean sourceProtected;// 原图保护
	private String style;// 样式

	/**
	 * 七牛云推荐的分页条数
	 */
	private static final int RECOMMEND_LIMIT = 100;

	private final String bucket;
	private final Auth auth;

	private static final int FILE_NOT_EXISTS_ERROR_CODE = 612;// 文件不存在错误码

	private static final String IMAGE_INFO_PARAM = "?imageInfo";

	public QiniuFileStore(int id, String name, QiniuConfig config) {
		if (Validators.isEmptyOrNull(config.getAccessKey(), true)
				|| Validators.isEmptyOrNull(config.getSecretKey(), true)) {
			throw new IllegalArgumentException("请提供accessKey和secretKey");
		}

		if (Validators.isEmptyOrNull(config.getBucket(), true)) {
			throw new IllegalArgumentException("请提供bucket");
		}

		String urlPrefix = config.getUrlPrefix();

		if (Validators.isEmptyOrNull(urlPrefix, true)) {
			throw new IllegalArgumentException("外链域名不能为空");
		}
		if (!UrlUtils.isAbsoluteUrl(urlPrefix)) {
			throw new IllegalArgumentException("外链域名必须是一个绝对路径");
		}
		if (!urlPrefix.endsWith("/")) {
			urlPrefix += "/";
		}

		boolean sourceProtected = config.isSourceProtected();

		String style = config.getStyle();
		Character styleSplitChar = config.getStyleSplitChar();

		if (sourceProtected) {
			if (style == null) {
				throw new IllegalArgumentException("开启了原图保护之后请指定一个默认的样式名");
			}
			if (styleSplitChar == null) {
				styleSplitChar = '-';
			}
		}

		this.id = id;
		this.name = name;

		this.auth = Auth.create(config.getAccessKey(), config.getSecretKey());
		this.bucket = config.getBucket();

		if (config.getSmallSize() != null) {
			this.smallResize = new Resize(config.getSmallSize());
		}

		if (config.getMiddleSize() != null) {
			this.middleResize = new Resize(config.getMiddleSize());
		}

		if (config.getLargeSize() != null) {
			this.largeResize = new Resize(config.getLargeSize());
		}

		this.readOnly = config.isReadonly();
		this.styleSplitChar = styleSplitChar;
		this.style = style;
		this.sourceProtected = sourceProtected;
		this.urlPrefix = urlPrefix;
	}

	@Override
	public final CommonFile store(String key, MultipartFile multipartFile) throws LogicException {
		if (!delete(key)) {
			throw new LogicException("file.store.exists", "文件" + key + "已经存在", key);
		}
		String originalFilename = multipartFile.getOriginalFilename();
		String extension = FileUtils.getFileExtension(originalFilename);
		Path tmp = FileUtils.appTemp(extension);
		try (InputStream is = multipartFile.getInputStream()) {
			Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		CommonFile cf = new CommonFile();

		String vkey = FileUtils.cleanPath(key);
		doUpload(vkey, tmp);

		cf.setExtension(extension);

		if (ImageHelper.isSystemAllowedImage(extension)) {
			try {
				ImageInfo ii = this.readImage(vkey);
				cf.setExtension(ii.getExtension());
			} catch (IOException e) {
				LOGGER.debug(e.getMessage(), e);
				throw new LogicException(new Message("image.corrupt", "不是正确的图片文件或者图片已经损坏"));
			}
		}

		cf.setOriginalFilename(originalFilename);
		cf.setSize(multipartFile.getSize());
		cf.setStore(id);
		return cf;
	}

	private void doUpload(String vkey, Path tmp) {
		try {
			upload(vkey, tmp);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	protected void upload(String key, Path file) throws IOException {
		UploadManager uploadManager = new UploadManager();
		try {
			Response resp = uploadManager.put(file.toFile(), key, getUpToken());
			if (!resp.isOK()) {
				throw new IOException("七牛云上传失败，异常信息:" + resp.toString() + ",响应信息:" + resp.bodyString());
			}
		} catch (QiniuException e) {
			Response r = e.response;
			try {
				throw new IOException("七牛云上传失败，异常信息:" + r.toString() + ",响应信息:" + r.bodyString(), e);
			} catch (QiniuException e1) {
				LOGGER.debug(e1.getMessage(), e1);
			}
		}
	}

	// 简单上传，使用默认策略，只需要设置上传的空间名就可以了
	protected String getUpToken() {
		return auth.uploadToken(bucket);
	}

	/**
	 * 读取图片信息
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 *             图片损坏或者格式不被接受
	 */
	protected ImageInfo readImage(String key) throws IOException {
		String json = Resources.readResourceToString(new UrlResource(urlPrefix + key + IMAGE_INFO_PARAM));
		ExpressionExecutor executor = Jsons.readJson(json);
		if (executor.isNull()) {
			throw new IOException("无法将结果转化为json信息:" + json);
		}
		try {
			String format = executor.execute("format").orElseThrow(() -> new SystemException("无法获取图片的格式：" + json));
			Integer width = Integer
					.parseInt(executor.execute("width").orElseThrow(() -> new SystemException("无法获取图片的宽度：" + json)));
			Integer height = Integer
					.parseInt(executor.execute("height").orElseThrow(() -> new SystemException("无法获取图片的高度：" + json)));
			return new ImageInfo(width, height, format);
		} catch (Exception e) {
			throw new IOException("获取图片信息失败:" + json, e);
		}
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public final boolean deleteBatch(String key) {
		String vkey = FileUtils.cleanPath(key);
		return doDeleteBatch(vkey);
	}

	@Override
	public final boolean delete(String key) {
		String vkey = FileUtils.cleanPath(key);
		return doDelete(vkey);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public boolean readOnly() {
		return readOnly;
	}

	@Override
	public boolean move(String oldPath, String path) {
		String vo = FileUtils.cleanPath(oldPath);
		String vp = FileUtils.cleanPath(path);
		if (doCopy(vo, vp)) {
			if (delete(vo)) {
				return true;
			} else {
				delete(vp);
			}
		}
		return false;
	}

	@Override
	public final boolean copy(String oldPath, String path) {
		String vo = FileUtils.cleanPath(oldPath);
		String vp = FileUtils.cleanPath(path);
		return doCopy(vo, vp);
	}

	protected boolean doCopy(String oldPath, String path) {
		try {
			new BucketManager(auth).copy(bucket, oldPath, bucket, path);
			return true;
		} catch (QiniuException e) {
			try {
				Response r = e.response;
				LOGGER.error("七牛云拷贝文件失败，异常信息:" + r.toString() + ",响应信息:" + r.bodyString(), e);
			} catch (QiniuException e1) {
				LOGGER.debug(e1.getMessage(), e1);
			}
		}
		return false;
	}

	protected boolean doDelete(String key) {
		boolean flag = false;
		BucketManager bucketManager = new BucketManager(auth);
		try {
			bucketManager.delete(bucket, key);
			flag = true;
		} catch (QiniuException e) {
			Response r = e.response;
			if (r.statusCode == FILE_NOT_EXISTS_ERROR_CODE) {
				flag = true;
			} else {
				try {
					LOGGER.error("七牛云删除失败，异常信息:" + r.toString() + ",响应信息:" + r.bodyString(), e);
				} catch (QiniuException e1) {
					LOGGER.debug(e1.getMessage(), e1);
				}
			}
		}
		return flag;
	}

	@Override
	public String getUrl(String key) {
		String url = urlPrefix + key;
		if (isSystemAllowedImage(key) && sourceProtected) {
			return url + styleSplitChar + style;
		}
		return url;
	}

	@Override
	public Optional<ThumbnailUrl> getThumbnailUrl(String key) {

		if (isSystemAllowedImage(key)) {
			if (sourceProtected) {
				// 只能采用样式访问
				String url = urlPrefix + key + styleSplitChar + style;
				return Optional.of(new QiniuThumbnailUrl(url, url, url, key));
			} else {
				String small = buildThumbnailUrl(key, smallResize);
				String middle = buildThumbnailUrl(key, middleResize);
				String large = buildThumbnailUrl(key, largeResize);
				return Optional.of(new QiniuThumbnailUrl(small, middle, large, key));
			}
		}
		return Optional.empty();
	}

	private final class QiniuThumbnailUrl extends ThumbnailUrl {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String key;

		private QiniuThumbnailUrl(String small, String middle, String large, String key) {
			super(small, middle, large);
			this.key = key;
		}

		@Override
		public String getThumbUrl(int width, int height, boolean keepRatio) {
			return buildThumbnailUrl(key, new Resize(width, height, keepRatio));
		}

		@Override
		public String getThumbUrl(int size) {
			return buildThumbnailUrl(key, new Resize(size));
		}

	}

	protected boolean doDeleteBatch(String key) {
		try {
			List<String> keys = new ArrayList<>();
			BucketManager bucketManager = new BucketManager(auth);
			FileListing fileListing = bucketManager.listFiles(bucket, key + '/', null, RECOMMEND_LIMIT, null);

			do {
				FileInfo[] items = fileListing.items;
				if (items != null && items.length > 0) {
					for (FileInfo fileInfo : items) {
						keys.add(fileInfo.key);
					}
				}
				fileListing = bucketManager.listFiles(bucket, key + '/', fileListing.marker, RECOMMEND_LIMIT, null);
			} while (!fileListing.isEOF());

			if (keys.isEmpty()) {
				return true;
			}

			Batch batch = new Batch();
			batch.delete(bucket, keys.toArray(String[]::new));
			return bucketManager.batch(batch).isOK();
		} catch (QiniuException e) {
			// 捕获异常信息
			Response r = e.response;
			LOGGER.error(r.toString(), e);
		}
		return false;
	}

	/**
	 * 构造缩略图信息地址
	 * 
	 * @param key
	 *            key
	 * @param resize
	 *            缩放信息，可能为null
	 * @return
	 */
	protected String buildThumbnailUrl(String key, Resize resize) {
		return urlPrefix + key + buildResizeParam(resize).map(param -> "?" + param).orElse("");
	}

	/**
	 * {@link Resize#isKeepRatio()}设置无效
	 * 
	 * @return
	 */
	protected Optional<String> buildResizeParam(Resize resize) {
		String result = null;
		if (resize != null) {
			if (resize.getSize() != null) {
				result = "imageView2/2/w/" + resize.getSize() + "/h/" + resize.getSize();
			} else if (resize.getWidth() == 0 && resize.getHeight() == 0) {
				result = null;
			} else if (resize.getWidth() == 0) {
				result = "imageView2/2/h/" + resize.getHeight();
			} else if (resize.getHeight() == 0) {
				result = "imageView2/2/w/" + resize.getWidth();
			} else {
				result = "imageView2/2/w/" + resize.getWidth() + "/h/" + resize.getHeight();
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public boolean canStore(MultipartFile multipartFile) {
		return true;// can store any file
	}

	protected final boolean isSystemAllowedImage(String key) {
		return ImageHelper.isSystemAllowedImage(FileUtils.getFileExtension(key));
	}

	public void setSmallResize(Resize smallResize) {
		this.smallResize = smallResize;
	}

	public void setMiddleResize(Resize middleResize) {
		this.middleResize = middleResize;
	}

	public void setLargeResize(Resize largeResize) {
		this.largeResize = largeResize;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}
