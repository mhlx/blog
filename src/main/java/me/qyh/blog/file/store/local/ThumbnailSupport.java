/*
 * Copyright 2016 qyh.me
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.ImageHelper;
import me.qyh.blog.file.store.Resize;
import me.qyh.blog.file.store.ResizeValidator;
import me.qyh.blog.file.store.ThumbnailUrl;

/**
 * 可以生成文件缩略图的存储器
 * 
 * @author Administrator
 *
 */
public abstract class ThumbnailSupport extends LocalResourceRequestHandlerFileStore {

	protected final Logger logger = LoggerFactory.getLogger(ThumbnailSupport.class);

	private static final String WEBP_ACCEPT = "image/webp";
	private static final char CONCAT_CHAR = 'X';
	private static final char FORCE_CHAR = '!';

	private static final String NO_WEBP = "nowebp";

	private ResizeValidator resizeValidator;

	private boolean supportWebp;

	private String thumbAbsPath;
	private Path thumbAbsFolder;

	protected Resize smallResize;
	protected Resize middleResize;
	protected Resize largeResize;

	@Autowired
	protected Thumbnailator thumbnailator;

	public ThumbnailSupport(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}

	@Override
	public CommonFile store(String key, MultipartFile mf) throws LogicException {
		Path dest = FileUtils.sub(absFolder, key);
		checkFileStoreable(dest);
		return doStore(dest, key, mf);
	}

	protected CommonFile doStore(Path dest, String key, MultipartFile mf) throws LogicException {
		String originalFilename = mf.getOriginalFilename();
		try {
			FileUtils.forceMkdir(dest.getParent());
			try (InputStream is = mf.getInputStream()) {
				Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		CommonFile cf = new CommonFile();
		cf.setExtension(FileUtils.getFileExtension(originalFilename));
		cf.setSize(mf.getSize());
		cf.setStore(id);
		cf.setOriginalFilename(originalFilename);

		return cf;
	}

	private void checkFileStoreable(Path dest) throws LogicException {
		if (FileUtils.exists(dest) && !FileUtils.deleteQuietly(dest)) {
			String absPath = dest.toAbsolutePath().toString();
			throw new LogicException("file.store.exists", "文件" + absPath + "已经存在", absPath);
		}
		if (FileUtils.isSub(dest, thumbAbsFolder)) {
			String absPath = dest.toAbsolutePath().toString();
			throw new LogicException("file.inThumb", "文件" + absPath + "不能被存放在缩略图文件夹下", absPath);
		}
	}

	@Override
	protected Resource findResource(HttpServletRequest request) throws IOException {
		return getPath(request).flatMap(path -> findResource(path, request)).orElse(null);
	}

	/**
	 * 当访问路径指向原始文件时，处理这个原始文件
	 * 
	 * @return 向客户端返回的资源
	 */
	protected abstract Optional<Resource> handleOriginalFile(String key, Path path, HttpServletRequest request);

	/**
	 * 获取文件的封面
	 * 
	 * @param original
	 *            原始文件
	 */
	protected abstract void extraPoster(Path original, Path poster) throws Exception;

	private Optional<Resource> findResource(String path, HttpServletRequest request) {
		// 判断是否是原图
		Optional<Path> optionaLocalFile = super.getFile(path);
		if (optionaLocalFile.isPresent()) {
			return handleOriginalFile(path, optionaLocalFile.get(), request);
		}

		// 原图不存在，从链接中获取缩放信息
		Optional<Resize> optionalResize = getResizeFromPath(path);
		if (!optionalResize.isPresent()) {
			// 如果连接中不包含缩略图信息
			return Optional.empty();
		}
		Resize resize = optionalResize.get();
		String sourcePath = getSourcePathByResizePath(path);
		String ext = FileUtils.getFileExtension(sourcePath);
		// 构造缩略图路径
		Optional<String> optionalThumbPath = getThumbPath(ext, path, request);

		// 如果缩略图路径无法被接受
		if (!optionalThumbPath.isPresent()) {
			return Optional.empty();
		}

		String thumbPath = optionalThumbPath.get();
		// 缩略图是否已经存在
		Path file = findThumbByPath(thumbPath);
		// 缩略图不存在，寻找原图
		if (!FileUtils.exists(file)) {

			Optional<Path> optionalFile = super.getFile(sourcePath);
			// 源文件也不存在
			if (!optionalFile.isPresent()) {
				return Optional.empty();
			}
			Path local = optionalFile.get();
			Path poster;

			if (ImageHelper.isSystemAllowedImage(ext)) {
				poster = local;
			} else {
				poster = file.getParent().resolve(FileUtils.getNameWithoutExtension(local.getFileName().toString()) +
				// 外加个@符号，防止跟缩略图文件冲突
						"@." + ImageHelper.PNG);

				if (!FileUtils.exists(poster)) {
					try {
						extraPoster(local, poster);
					} catch (Exception e) {
						logger.debug(e.getMessage(), e);
					}
				}

				if (!FileUtils.isRegularFile(poster)) {
					return Optional.of(new PathResource(local));
				}

			}

			try {
				thumbnailator.doResize(poster, resize, file);
				return FileUtils.exists(file) ? Optional.of(new PathResource(file)) : Optional.empty();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return Optional.empty();
			}

		} else {
			// 直接返回缩略图
			return Optional.of(new PathResource(file));
		}
	}

	@Override
	public boolean delete(String key) {
		boolean flag = super.delete(key);
		if (flag) {
			Path thumbDir = FileUtils.sub(thumbAbsFolder, key);
			if (FileUtils.exists(thumbDir)) {
				flag = FileUtils.deleteQuietly(thumbDir);
			}
		}
		return flag;
	}

	@Override
	public boolean deleteBatch(String key) {
		return delete(key);
	}

	@Override
	public boolean move(String oldPath, String path) {
		if (super.move(oldPath, path)) {
			FileUtils.deleteQuietly(FileUtils.sub(thumbAbsFolder, oldPath));
			return true;
		}
		return false;
	}

	@Override
	public Optional<ThumbnailUrl> getThumbnailUrl(String key) {
		return Optional.of(new _ThumbnailUrl(buildResizePath(smallResize, key), buildResizePath(middleResize, key),
				buildResizePath(largeResize, key), key));
	}

	// 不能匿名内部类，否则gson无法write
	private final class _ThumbnailUrl extends ThumbnailUrl {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String key;

		private _ThumbnailUrl(String small, String middle, String large, String key) {
			super(small, middle, large);
			this.key = key;
		}

		@Override
		public String getThumbUrl(int width, int height, boolean keepRatio) {
			return buildResizePath(new Resize(width, height, keepRatio), key);
		}

		@Override
		public String getThumbUrl(int size) {
			return buildResizePath(new Resize(size), key);
		}

	}

	protected String buildResizePath(Resize resize, String key) {
		String path = key;
		if (!key.startsWith("/")) {
			path = "/" + key;
		}
		if (resize == null) {
			return getUrl(path);
		}

		return urlPrefix + "/" + FileUtils.cleanPath(generateResizePathFromPath(resize, path));
	}

	@Override
	protected void moreAfterPropertiesSet() {

		validateResize(smallResize);
		validateResize(middleResize);
		validateResize(largeResize);

		if (thumbAbsPath == null) {
			throw new SystemException("缩略图存储路径不能为null");
		}

		super.setLocations(List.of(new PathResource(Paths.get(thumbAbsPath)), new PathResource(Paths.get(absPath))));

		if (!thumbnailator.supportWebp()) {
			supportWebp = false;
		}

		thumbAbsFolder = Paths.get(thumbAbsPath);
		FileUtils.forceMkdir(thumbAbsFolder);

		if (resizeValidator == null) {
			resizeValidator = resize -> true;
		}
	}

	private void validateResize(Resize resize) {
		if (resize != null && !resizeValidator.valid(resize)) {
			throw new SystemException("默认缩放尺寸：" + resize + "无法被接受！请调整ResizeUrlParser");
		}
	}

	private Path findThumbByPath(String path) {
		String southPath = getSourcePathByResizePath(path);
		Path thumbDir = FileUtils.sub(thumbAbsFolder, southPath);
		String name = new File(path).getName();
		return FileUtils.sub(thumbDir, name);
	}

	protected boolean supportWebp(HttpServletRequest request) {
		if (!supportWebp) {
			return false;
		}
		if (request.getParameter(NO_WEBP) != null) {
			return false;
		}
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains(WEBP_ACCEPT);
	}

	protected String generateResizePathFromPath(Resize resize, String path) {
		if (!resizeValidator.valid(resize)) {
			return path;
		}
		return StringUtils.cleanPath(path + "/" + getThumname(resize));
	}

	protected Optional<Resize> getResizeFromPath(String path) {
		Resize resize;
		String baseName = FileUtils.getNameWithoutExtension(path);
		try {
			if (baseName.indexOf(CONCAT_CHAR) != -1) {
				boolean keepRatio = true;
				if (baseName.endsWith(Character.toString(FORCE_CHAR))) {
					keepRatio = false;
					baseName = baseName.substring(0, baseName.length() - 1);
				}
				if (baseName.startsWith(Character.toString(CONCAT_CHAR))) {
					baseName = baseName.substring(1, baseName.length());
					Integer h = Integer.valueOf(baseName);
					resize = new Resize();
					resize.setHeight(h);
					resize.setKeepRatio(keepRatio);
				} else if (baseName.endsWith(Character.toString(CONCAT_CHAR))) {
					baseName = baseName.substring(0, baseName.length() - 1);
					Integer w = Integer.valueOf(baseName);
					resize = new Resize();
					resize.setWidth(w);
					resize.setKeepRatio(keepRatio);
				} else {
					String[] splits = baseName.split(Character.toString(CONCAT_CHAR));
					if (splits.length != 2) {
						return Optional.empty();
					} else {
						Integer w = Integer.valueOf(splits[0]);
						Integer h = Integer.valueOf(splits[1]);
						resize = new Resize(w, h, keepRatio);
					}
				}
			} else {
				resize = new Resize(Integer.valueOf(baseName));
			}
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
		return resizeValidator.valid(resize) ? Optional.of(resize) : Optional.empty();
	}

	private String getThumname(Resize resize) {
		StringBuilder sb = new StringBuilder();
		if (resize.getSize() != null) {
			sb.append(resize.getSize());
		} else {
			sb.append((resize.getWidth() <= 0) ? "" : resize.getWidth());
			sb.append(CONCAT_CHAR);
			sb.append(resize.getHeight() <= 0 ? "" : resize.getHeight());
			sb.append(resize.isKeepRatio() ? "" : FORCE_CHAR);
		}
		return sb.toString();
	}

	protected String getSourcePathByResizePath(String path) {
		String sourcePath = path;
		int index = path.lastIndexOf('/');
		if (index != -1) {
			sourcePath = path.substring(0, index);
		}
		return FileUtils.cleanPath(sourcePath);
	}

	/**
	 * 获取缩略图格式
	 * 
	 * @param sourceExt
	 * @param ext
	 *            访问连接后缀
	 * @param request
	 *            请求
	 * @return
	 */
	private Optional<String> getThumbPath(String sourceExt, String path, HttpServletRequest request) {
		boolean supportWebp = supportWebp(request);
		String ext = FileUtils.getFileExtension(path);
		boolean extEmpty = ext.strip().isEmpty();
		if (extEmpty) {
			return Optional.of(path + "." + (supportWebp ? ImageHelper.WEBP : ImageHelper.JPEG));
		} else {
			// 如果为png并且原图可能为透明
			if (ImageHelper.isPNG(ext) && ImageHelper.maybeTransparentBg(sourceExt)) {
				String basePath = path.substring(0, path.length() - ext.length() - 1);
				return Optional.of(basePath + "." + ImageHelper.PNG);
			}
		}
		return Optional.empty();
	}

	protected Path getPoster(String key) {
		Path thumbRoot = FileUtils.sub(thumbAbsFolder, key);
		String name = FileUtils.getNameWithoutExtension(key);
		return thumbRoot.resolve(name + "@." + ImageHelper.PNG);
	}

	public void setThumbAbsPath(String thumbAbsPath) {
		this.thumbAbsPath = thumbAbsPath;
	}

	public void setResizeValidator(ResizeValidator resizeValidator) {
		this.resizeValidator = resizeValidator;
	}

	public void setSupportWebp(boolean supportWebp) {
		this.supportWebp = supportWebp;
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

	protected final ImageHelper getImageHelper() {
		return thumbnailator.getImageHelper();
	}
}
