package me.qyh.blog.file.store;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.madgag.gif.fmsware.GifDecoder;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.core.util.Validators;

/**
 * 图片处理类，基于{@link http://www.graphicsmagick.org/}，可以用来处理PNG,JPEG,GIF,WEBP等多种格式
 * 
 * @see http://im4java.sourceforge.net/docs/dev-guide.html
 * @author Administrator
 *
 */
public class GraphicsMagickImageHelper extends ImageHelper implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GraphicsMagickImageHelper.class);

	/**
	 * 在windows环境下，必须设置这个路径
	 */
	private String magickPath;

	private static final boolean WINDOWS = File.separatorChar == '\\';

	private boolean doInterlace;

	/**
	 * <p>
	 * JPEG格式图片质量
	 * </p>
	 */
	private double quality = 75d;

	/**
	 * 设置pngQuant的根目录路径(windows)，用于压缩PNG图片
	 * 
	 * @since 7.0
	 */
	private String pngQuantDirPath;

	private boolean pngQuantEnable;

	/**
	 * 用于处理gif图片
	 * 
	 * @since 7.1.3
	 */
	private String gifsicleDirPath;
	private boolean gifsicleEnable;

	@Override
	protected void doResize(Resize resize, Path src, Path dest) throws IOException {
		boolean gif = isGIF(FileUtils.getFileExtension(src));
		if (gif && gifsicleEnable) {
			resizeWithGifsicle(resize, src, dest);
			return;
		}

		IMOperation op = new IMOperation();
		op.addImage();
		op.strip();
		setResize(resize, op);
		String ext = FileUtils.getFileExtension(dest);
		if (!maybeTransparentBg(ext)) {
			setWhiteBg(op);
		}
		boolean interlace = interlace(dest);
		if (interlace) {
			op.interlace("Line");
		}
		/**
		 * @since 5.9 防止某些图片旋转90度
		 */
		op.autoOrient();
		if (isJPEG(ext)) {
			op.quality(quality);
		}
		op.addImage();

		Path temp = FileUtils.appTemp(ext);
		try {
			run(op, src.toAbsolutePath().toString(), temp.toAbsolutePath().toString());
		} catch (IOException e) {
			// 如果原图是gif图像
			if (gif) {
				doResize(resize, getGifCoverUseJava(src), dest);
				return;
			} else {
				throw e;
			}
		}
		compress(temp, ext);
		synchronized (this) {
			FileUtils.move(temp, dest);
		}
	}

	private void resizeWithGifsicle(Resize resize, Path gif, Path dest) throws IOException {
		Path temp = FileUtils.appTemp(GIF);
		try {
			String cmdPath = WINDOWS ? new File(gifsicleDirPath, "gifsicle").getCanonicalPath() : "gifsicle";
			List<String> commands = new ArrayList<>(List.of(cmdPath, "--no-warnings", "-careful"));

			if (resize.getSize() != null) {
				int size = resize.getSize();
				commands.add("--resize-fit");
				commands.add(size + "x" + size);
			} else {
				if (!resize.isKeepRatio()) {
					commands.add("--resize");
					commands.add(resize.getWidth() + "x" + resize.getHeight());
				} else {
					if (resize.getWidth() <= 0) {
						commands.add("--resize-height");
						commands.add(String.valueOf(resize.getHeight()));
					} else if (resize.getHeight() <= 0) {
						commands.add("--resize-width");
						commands.add(String.valueOf(resize.getWidth()));
					} else {
						commands.add("--resize-fit");
						commands.add(resize.getWidth() + "x" + resize.getHeight());
					}
				}
			}
			commands.addAll(List.of(gif.toString(), "-o", temp.toString()));
			ProcessUtils.runProcess(commands, true);
		} catch (IOException e) {
			throw new IOException(e);
		}
		synchronized (this) {
			FileUtils.move(temp, dest);
		}
	}

	@Override
	protected ImageInfo doRead(Path file) throws IOException {
		boolean gif = isGIF(FileUtils.getFileExtension(file));
		if (gif && gifsicleEnable) {
			String cmdPath = WINDOWS ? new File(gifsicleDirPath, "gifsicle").getCanonicalPath() : "gifsicle";
			List<String> commands = List.of(cmdPath, "--sinfo", file.toString());
			try {
				String info = ProcessUtils.runProcess(commands, true);
				try {
					String[] size = StringUtils.substringBetween(info, "screen ", "\r\n").split("x");
					return new ImageInfo(Integer.parseInt(size[0]), Integer.parseInt(size[1]), GIF);
				} catch (Exception e) {
					// read fail
					logger.warn("read gif size fail", e);
				}
				return new ImageInfo(-1, -1, GIF);
			} catch (IOException e) {
				// maybe not gif file
				// continue;
			}
		}
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
			if (isGIF(FileUtils.getFileExtension(file))) {
				return readGif(file);
			} else {
				throw e;
			}
		}
	}

	protected void compress(Path dest, String ext) throws IOException {
		if (isPNG(ext) && pngQuantEnable) {
			try {
				String cmdPath = WINDOWS ? new File(pngQuantDirPath, "pngquant").getCanonicalPath() : "pngquant";
				ProcessUtils.runProcess(Arrays.asList(cmdPath, "-f", "-o", dest.toString(), dest.toString()), false);
			} catch (IOException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (quality < 0 || quality > 100) {
			throw new SystemException("图片质量应该在(0~100]之间");
		}
		if (WINDOWS) {
			if (!Validators.isEmptyOrNull(magickPath, true)) {
				ProcessStarter.setGlobalSearchPath(magickPath);
			}
			pngQuantEnable = !Validators.isEmptyOrNull(pngQuantDirPath, true);
			gifsicleEnable = !Validators.isEmptyOrNull(gifsicleDirPath, true);
		} else {
			try {
				ProcessUtils.runProcess(Arrays.asList("pngquant", "--version"), false);
				pngQuantEnable = true;
			} catch (IOException e) {
				pngQuantEnable = false;
			}
			try {
				ProcessUtils.runProcess(Arrays.asList("gifsicle", "--help"), true);
				gifsicleEnable = true;
			} catch (IOException e) {
				gifsicleEnable = false;
			}
		}
	}

	private boolean interlace(Path dest) {
		if (!doInterlace) {
			return false;
		}
		// https://stackoverflow.com/questions/13449314/when-to-interlace-an-image
		String ext = FileUtils.getFileExtension(dest);
		return isJPEG(ext);
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

	@Override
	public boolean supportGifsicle() {
		return gifsicleEnable;
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

	public void setPngQuantDirPath(String pngQuantDirPath) {
		this.pngQuantDirPath = pngQuantDirPath;
	}

	public void setGifsicleDirPath(String gifsicleDirPath) {
		this.gifsicleDirPath = gifsicleDirPath;
	}

}
