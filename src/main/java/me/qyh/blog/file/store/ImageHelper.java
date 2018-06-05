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
package me.qyh.blog.file.store;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;

/**
 * 图片辅助类，用来处理图片格式的转化，缩放以及图片的读取
 * 
 * @author Administrator
 *
 */
public abstract class ImageHelper {

	public static final String GIF = "gif";
	public static final String JPEG = "jpeg";
	public static final String JPG = "jpg";
	public static final String PNG = "png";
	public static final String WEBP = "webp";

	private static final String[] ALLOWED_IMG_EXTENSIONS = { GIF, JPEG, JPG, PNG };

	/**
	 * <p>
	 * 用来指定缩放后的文件信息,如果指定了纵横比但同时指定了缩略图宽度和高度，将会以宽度或者长度为准(具体图片不同),如果只希望将长度(或宽度进行缩放) ，
	 * 那么只要将另一个长度置位 <=0就可以了 如果不保持纵横比同时没有指定宽度和高度(都<=0)将返回原图链接<br/>
	 * <strong>总是缩放(即比原图小)</strong>
	 * </p>
	 * 
	 * @param resize
	 *            缩放尺寸
	 * @param src
	 *            原文件
	 * @param dest
	 *            目标文件
	 * @throws IOException
	 *             文件读写失败
	 */
	public final void resize(Resize resize, Path src, Path dest) throws IOException {
		formatCheck(FileUtils.getFileExtension(src));
		formatCheck(FileUtils.getFileExtension(dest));
		doResize(resize, src, dest);
	}

	/**
	 * 读取图片信息
	 * 
	 * @param file
	 *            图片文件
	 * @return
	 * @throws IOException
	 *             文件读取失败
	 */
	public final ImageInfo read(Path file) throws IOException {
		formatCheck(FileUtils.getFileExtension(file));
		return doRead(file);

	}

	protected abstract void doResize(Resize resize, Path src, Path dest) throws IOException;

	protected abstract ImageInfo doRead(Path file) throws IOException;

	/**
	 * 是否支持webp格式
	 * 
	 * @return
	 */
	public abstract boolean supportWebp();

	/**
	 * @since 5.9
	 * @return
	 */
	public boolean supportAnimatedWebp() {
		return false;
	}

	/**
	 * @since 5.9
	 * @param gif
	 * @throws IOException
	 */
	public void makeAnimatedWebp(AnimatedWebpConfig config, Path gif, Path dest) throws IOException {
		if (!supportAnimatedWebp()) {
			throw new SystemException("not support");
		}
		formatCheck(FileUtils.getFileExtension(gif));
		formatCheck(FileUtils.getFileExtension(dest));
		doMakeAnimatedWebp(config, gif, dest);
	}

	protected abstract void doMakeAnimatedWebp(AnimatedWebpConfig config, Path gif, Path dest) throws IOException;

	/**
	 * 图片信息
	 * 
	 * @author Administrator
	 *
	 */
	public static final class ImageInfo {
		private final int width;
		private final int height;
		private final String extension;// 图片真实后缀

		/**
		 * 
		 * @param width
		 *            宽
		 * @param height
		 *            高
		 * @param extension
		 *            实际后缀
		 */
		public ImageInfo(int width, int height, String extension) {
			super();
			this.width = width;
			this.height = height;
			this.extension = extension;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public String getExtension() {
			return extension;
		}

		@Override
		public String toString() {
			return "ImageInfo [width=" + width + ", height=" + height + ", extension=" + extension + "]";
		}
	}

	/**
	 * 判断图片是否为jpeg格式的图片
	 * 
	 * @param extension
	 *            图片后缀
	 * @return 是: true ,否:false
	 */
	public static boolean isJPEG(String extension) {
		return JPEG.equalsIgnoreCase(extension) || JPG.equalsIgnoreCase(extension);
	}

	/**
	 * 判断图片是否为webp格式的图片
	 * 
	 * @param extension
	 *            图片后缀
	 * @return 是: true ,否:false
	 */
	public static boolean isWEBP(String extension) {
		return WEBP.equalsIgnoreCase(extension);
	}

	/**
	 * 判断图片是否为png格式的图片
	 * 
	 * @param extension
	 *            图片后缀
	 * @return 是: true ,否:false
	 */
	public static boolean isPNG(String extension) {
		return PNG.equalsIgnoreCase(extension);
	}

	/**
	 * 判断图片是否为gif格式的图片
	 * 
	 * @param extension
	 *            图片后缀
	 * @return 是: true ,否:false
	 */
	public static boolean isGIF(String extension) {
		return GIF.equalsIgnoreCase(extension);
	}

	/**
	 * 判断图片可能是允许透明的
	 * 
	 * @param extension
	 *            图片后缀
	 * @return 是:true,否:false
	 */
	public static boolean maybeTransparentBg(String extension) {
		return isPNG(extension) || isGIF(extension) || isWEBP(extension);
	}

	/**
	 * 判断是否使是系统允许的图片格式
	 * <p>
	 * <b>只接受gif,jpg|jpeg,png格式的图片</b>
	 * </p>
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isSystemAllowedImage(String ext) {
		if (ext != null) {
			String lower = ext.toLowerCase();
			return Arrays.stream(ALLOWED_IMG_EXTENSIONS).anyMatch(lower::equals);
		}
		return false;
	}

	private void formatCheck(String extension) throws IOException {
		if (isWEBP(extension)) {
			if (!supportWebp()) {
				throw new IOException("文件格式" + extension + "不被支持");
			}
		} else if (!isSystemAllowedImage(extension)) {
			throw new IOException("文件格式" + extension + "不被支持");
		}
	}
}
