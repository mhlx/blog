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
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.AnimatedWebpConfig.Metadata;
import me.qyh.blog.file.store.ImageHelper;
import me.qyh.blog.file.store.ImageHelper.ImageInfo;
import me.qyh.blog.file.store.Resize;

/**
 * 本地图片存储，图片访问
 * 
 * @author Administrator
 *
 */
public class ImageResourceStore extends ThumbnailSupport {

	/**
	 * 原图保护
	 */
	private boolean sourceProtected;

	private AnimatedWebpConfigure animatedWebpConfigure;

	public ImageResourceStore(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}

	public ImageResourceStore() {
		this("image");
	}

	@Override
	public MultipartFile preHandler(MultipartFile file) throws LogicException {
		return new ImageMultipareFile(file);
	}

	@Override
	public Map<Message, String> getProperties(String key) {
		Optional<Path> path = super.getFile(key);
		if (path.isPresent()) {
			try {
				ImageInfo info = this.readImage(path.get());
				return Map.of(new Message("image.width", "图片宽度"), String.valueOf(info.getWidth()),
						new Message("image.height", "图片高度"), String.valueOf(info.getHeight()));
			} catch (LogicException e) {
				return Map.of();
			}
		}
		return super.getProperties(key);
	}

	@Override
	public CommonFile doStore(Path dest, String key, MultipartFile mf) throws LogicException {
		ImageMultipareFile file;
		if (mf instanceof ImageMultipareFile) {
			file = (ImageMultipareFile) mf;
		} else {
			file = new ImageMultipareFile(mf);
		}
		ImageInfo ii = file.getInfo();
		String extension = ii.getExtension();
		try {
			FileUtils.forceMkdir(dest.getParent());
			FileUtils.move(file.getTmp(), dest);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		CommonFile cf = new CommonFile();
		cf.setExtension(extension);
		cf.setSize(mf.getSize());
		cf.setStore(id);
		cf.setOriginalFilename(file.getOriginalFilename());

		return cf;
	}

	private ImageInfo readImage(Path tmp) throws LogicException {
		try {
			return getImageHelper().read(tmp);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			throw new LogicException("image.corrupt", "不是正确的图片文件或者图片已经损坏");
		}
	}

	@Override
	public final boolean canStore(MultipartFile multipartFile) {
		String ext = FileUtils.getFileExtension(multipartFile.getOriginalFilename());
		return ImageHelper.isSystemAllowedImage(ext);
	}

	@Override
	public String getUrl(String key) {
		if (sourceProtected) {
			if (ImageHelper.isGIF(FileUtils.getFileExtension(key))) {
				return super.getUrl(key);
			}
			Resize resize = largeResize == null ? (middleResize == null ? smallResize : middleResize) : largeResize;
			return buildResizePath(resize, key);
		} else {
			return super.getUrl(key);
		}
	}

	@Override
	protected Optional<Resource> handleOriginalFile(String key, Path path, HttpServletRequest request) {
		String ext = FileUtils.getFileExtension(path);
		if (ImageHelper.isGIF(ext)) {

			if (!supportWebp(request)) {
				return Optional.of(new PathResource(path));
			}

			Path animated = getAnimatedWebpLocation(path);
			if (FileUtils.exists(animated)) {
				return Optional.of(new PathResource(animated));
			}

			if (animatedWebpConfigure != null && getImageHelper().supportAnimatedWebp()) {

				Semaphore semaphore = animatedWebpConfigure.getSemaphore();
				try {
					semaphore.acquire();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SystemException(e.getMessage(), e);
				}
				try {
					getImageHelper().makeAnimatedWebp(animatedWebpConfigure.newAnimatedWebpConfig(), path, animated);
					return Optional.of(new PathResource(animated));
				} catch (IOException e) {
					logger.debug(e.getMessage(), e);
					return Optional.of(new PathResource(path));
				} finally {
					semaphore.release();
				}

			} else {
				return Optional.of(new PathResource(path));
			}
		}
		if (!sourceProtected) {
			return Optional.of(new PathResource(path));
		}
		return Optional.empty();
	}

	@Override
	protected void extraPoster(Path original, Path poster) {
		throw new SystemException("unaccepted !!!");
	}

	@Override
	public boolean delete(String key) {
		getFile(key).filter(path -> ImageHelper.isGIF(FileUtils.getFileExtension(path)))
				.ifPresent(path -> FileUtils.deleteQuietly(getAnimatedWebpLocation(path)));
		return super.delete(key);
	}

	@Override
	public void moreAfterPropertiesSet() {
		super.moreAfterPropertiesSet();
		if (animatedWebpConfigure != null) {
			int method = animatedWebpConfigure.getMethod();
			if (method < 0 || method > 6) {
				animatedWebpConfigure.setMethod(4);
			}
			float q = animatedWebpConfigure.getQ();
			if (q < 0F || q > 100F) {
				animatedWebpConfigure.setQ(75F);
			}

			if (animatedWebpConfigure.getMetadata() == null) {
				animatedWebpConfigure.setMetadata(Metadata.NONE);
			}
		}
	}

	protected Path getAnimatedWebpLocation(Path gif) {
		return gif.resolveSibling(gif.getFileName() + "." + ImageHelper.WEBP);
	}

	public void setSourceProtected(boolean sourceProtected) {
		this.sourceProtected = sourceProtected;
	}

	public void setAnimatedWebpConfigure(AnimatedWebpConfigure animatedWebpConfigure) {
		this.animatedWebpConfigure = animatedWebpConfigure;
	}

	protected class ImageMultipareFile implements MultipartFile {

		private final MultipartFile file;
		private final ImageInfo info;
		private final Path tmp;

		protected ImageMultipareFile(MultipartFile file) throws LogicException {
			super();
			this.file = file;
			this.tmp = FileUtils.appTemp(FileUtils.getFileExtension(file.getOriginalFilename()));
			try (InputStream is = file.getInputStream()) {
				Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new SystemException(e.getMessage(), e);
			}
			try {
				this.info = readImage(tmp);
			} catch (LogicException e) {
				FileUtils.deleteQuietly(tmp);
				throw e;
			}
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public String getOriginalFilename() {
			String originalFilename = file.getOriginalFilename();
			String name = FileUtils.getNameWithoutExtension(originalFilename);
			return name + "." + info.getExtension().toLowerCase();
		}

		@Override
		public String getContentType() {
			return "image/" + info.getExtension().toLowerCase();
		}

		@Override
		public boolean isEmpty() {
			return file.isEmpty();
		}

		@Override
		public long getSize() {
			return file.getSize();
		}

		@Override
		public byte[] getBytes() throws IOException {
			enableTmpExists();
			return Files.readAllBytes(tmp);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			enableTmpExists();
			return Files.newInputStream(tmp);
		}

		@Override
		public void transferTo(File dest) throws IOException, IllegalStateException {
			enableTmpExists();
			FileUtils.forceMkdir(dest.toPath().getParent());
			Files.copy(tmp, dest.toPath());
		}

		protected ImageInfo getInfo() {
			return info;
		}

		protected Path getTmp() {
			return tmp;
		}

		private void enableTmpExists() {
			if (!Files.exists(tmp)) {
				throw new IllegalStateException("File has been moved - cannot be read again");
			}
		}
	}
}
