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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.ImageCommand;
import org.im4java.core.Operation;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.ProcessStarter;
import org.springframework.beans.factory.InitializingBean;

import com.madgag.gif.fmsware.GifDecoder;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.store.local.ProcessException;

/**
 * 图片处理类，基于{@link http://www.graphicsmagick.org/}，可以用来处理PNG,JPEG,GIF,WEBP等多种格式
 * 
 * @see http://im4java.sourceforge.net/docs/dev-guide.html
 * @author Administrator
 *
 */
public class GraphicsMagickImageHelper extends ImageHelper implements InitializingBean {

	/**
	 * 在windows环境下，必须设置这个路径
	 */
	private String magickPath;

	private static final boolean WINDOWS = File.separatorChar == '\\';

	/**
	 * 如果为true，那么将会以渐进的方式显示出来，但在有些浏览器，例如EDGE则会先显示空白后再显示图片
	 * <p>
	 * 该选项会使图片增大
	 * </p>
	 */
	private boolean doInterlace = false;

	/**
	 * <p>
	 * JPEG格式图片质量
	 * </p>
	 */
	private double quality = 75d;

	@Override
	protected void doResize(Resize resize, Path src, Path dest) throws IOException {
		IMOperation op = new IMOperation();
		op.addImage();
		op.strip();
		setResize(resize, op);
		String ext = FileUtils.getFileExtension(dest);
		if (!maybeTransparentBg(ext)) {
			setWhiteBg(op);
		}
		if (interlace(dest)) {
			op.interlace("Line");
		}
		/**
		 * @since 5.9 防止某些图片旋转90度
		 */
		op.autoOrient();
		addCompressOp(op, ext);
		op.addImage();

		Path temp = FileUtils.appTemp(ext);
		try {
			run(op, src.toAbsolutePath().toString(), temp.toAbsolutePath().toString());
			// windows下，如果多个线程move到同一文件
			// 会出现 java.nio.file.AccessDeniedException异常
			synchronized (this) {
				FileUtils.move(temp, dest);
			}
		} catch (IOException e) {
			// 如果原图是gif图像
			if (isGIF(FileUtils.getFileExtension(src))) {
				doResize(resize, getGifCoverUseJava(src), dest);
			} else {
				throw e;
			}
		}
	}

	@Override
	protected ImageInfo doRead(Path file) throws IOException {
		IMOperation localIMOperation = new IMOperation();
		localIMOperation.ping();
		localIMOperation.format("%w\n%h\n%m\n");
		localIMOperation.addImage();
		ArrayListOutputConsumer localArrayListOutputConsumer = new ArrayListOutputConsumer();
		try {
			run(() -> {
				IdentifyCmd localIdentifyCmd = new IdentifyCmd(true);
				localIdentifyCmd.setOutputConsumer(localArrayListOutputConsumer);
				return localIdentifyCmd;
			}, localIMOperation, file.toAbsolutePath().toString());

			List<String> atts = localArrayListOutputConsumer.getOutput();
			Iterator<String> it = atts.iterator();
			return new ImageInfo(Integer.parseInt(it.next()), Integer.parseInt(it.next()), it.next());

		} catch (IOException e) {
			// 如果是GIF，尝试采用java读取
			// 如果原图是gif图像
			if (isGIF(FileUtils.getFileExtension(file))) {
				return readGif(file);
			} else {
				throw e;
			}
		}
	}

	protected void addCompressOp(IMOperation op, String ext) {
		if (isJPEG(ext)) {
			op.interlace("Plane");
			op.quality(quality);
		}
		if (isPNG(ext)) {
			op.define("png:compression-filter=2");
			op.define("png:compression-level=9");
			op.define("png:compression-strategy=1");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (quality < 0 || quality > 100) {
			throw new SystemException("图片质量应该在(0~100]之间");
		}
		if (WINDOWS && !Validators.isEmptyOrNull(magickPath, true)) {
			ProcessStarter.setGlobalSearchPath(magickPath);
		}
	}

	private boolean interlace(Path dest) {
		if (!doInterlace) {
			return false;
		}
		String ext = FileUtils.getFileExtension(dest);
		return isGIF(ext) || isPNG(ext) || isJPEG(ext);
	}

	private void setWhiteBg(IMOperation op) {
		op.background("rgb(255,255,255)");
		op.extent(0, 0);
		op.addRawArgs("+matte");
	}

	public void setDoInterlace(boolean doInterlace) {
		this.doInterlace = doInterlace;
	}

	public void setMagickPath(String magickPath) {
		this.magickPath = magickPath;
	}

	protected void setResize(Resize resize, IMOperation op) {
		if (resize.getSize() != null) {
			op.thumbnail(resize.getSize(), resize.getSize(), '>');
		} else {
			if (!resize.isKeepRatio()) {
				op.thumbnail(resize.getWidth(), resize.getHeight(), '!');
			} else {
				if (resize.getWidth() <= 0) {
					op.thumbnail(Integer.MAX_VALUE, resize.getHeight(), '>');
				} else if (resize.getHeight() <= 0) {
					op.thumbnail(resize.getWidth(), Integer.MAX_VALUE, '>');
				} else {
					op.thumbnail(resize.getWidth(), resize.getHeight(), '>');
				}
			}
		}
	}

	private void run(Supplier<ImageCommand> supplier, Operation operation, Object... args) throws IOException {
		try {
			supplier.get().run(operation, args);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemException(e.getMessage(), e);
		} catch (IM4JavaException e) {
			throw new IOException(e);
		}
	}

	private void run(Operation operation, Object... args) throws IOException {
		run(() -> new ConvertCmd(true), operation, args);
	}

	@Override
	public boolean supportWebp() {
		return true;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	/**
	 * 有些gif图片GM无法处理，此时尝试用java提取gif图的封面
	 * 
	 * @param gif
	 * @param _gif
	 * @return PNG格式的图片
	 * @throws IOException
	 */
	private Path getGifCoverUseJava(Path gif) throws IOException {
		GifDecoder gd = new GifDecoder();
		try (InputStream is = Files.newInputStream(gif)) {
			int flag = gd.read(is);
			if (flag != GifDecoder.STATUS_OK) {
				throw new IOException(gif + "文件无法获取封面");
			}
			Path tmp = FileUtils.appTemp(PNG);
			BufferedImage bi = gd.getFrame(0);
			synchronized (this) {
				ImageIO.write(bi, GIF, tmp.toFile());
			}
			return tmp;
		}
	}

	/**
	 * 有些gif图片GM无法处理，此时尝试用java读取gif信息
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private ImageInfo readGif(Path file) throws IOException {
		try (InputStream is = Files.newInputStream(file)) {
			GifDecoder gd = new GifDecoder();
			int flag = gd.read(is);
			if (flag != GifDecoder.STATUS_OK) {
				throw new IOException(file + "文件无法读取");
			}
			Dimension dim = gd.getFrameSize();
			return new ImageInfo(dim.width, dim.height, GIF);
		}
	}

	@Override
	public boolean supportAnimatedWebp() {
		return true;
	}

	/**
	 * @see https://developers.google.com/speed/webp/docs/gif2webp
	 */
	@Override
	protected void doMakeAnimatedWebp(AnimatedWebpConfig config, Path gif, Path dest) throws IOException {
		if (!supportAnimatedWebp()) {
			throw new SystemException("unsupport !!!");
		}
		Path tmp = FileUtils.appTemp(GIF);
		try {
			processAnimatedCommand(config, gif, tmp);

			synchronized (this) {
				FileUtils.move(tmp, dest);
			}

		} catch (ProcessException e) {
			throw new IOException(e.getMessage(), e);
		} finally {
			FileUtils.deleteQuietly(tmp);
		}
	}

	protected static List<String> buildCommand(AnimatedWebpConfig config, Path gif, Path tmp) {
		List<String> commandList = new ArrayList<>();
		commandList.add("gif2webp");
		if (config.isLossy()) {
			commandList.add("-lossy");
		}
		if (config.isMixed()) {
			commandList.add("-mixed");
		}

		commandList.add("-q");
		commandList.add(Float.toString(config.getQ()));

		commandList.add("-m");
		commandList.add(Integer.toString(config.getMethod()));

		commandList.add("-metadata");
		commandList.add(config.getMetadata().getValue());

		commandList.add(gif.toString());
		commandList.add("-o");
		commandList.add(tmp.toString());

		return commandList;
	}

	protected void processAnimatedCommand(AnimatedWebpConfig config, Path gif, Path dest) throws ProcessException {
		List<String> command = buildCommand(config, gif, dest);
		ProcessUtils.runProcess(command, 10, TimeUnit.SECONDS);
	}

}
