package me.qyh.blog.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StreamUtils;

/**
 * 媒体处理工具，支持GIF|PNG|JPEG图片的处理，以及MP4|MOV视频的处理
 * 
 * @author wwwqyhme
 *
 */
class MediaTool {

	public static final String PNG = "PNG";
	public static final String JPG = "JPG";
	public static final String JPEG = "JPEG";
	public static final String GIF = "GIF";
	public static final String WEBP = "WEBP";
	public static final String MP4 = "MP4";
	public static final String MOV = "MOV";

	private boolean gmEnable;
	private boolean pngQuantEnable;
	private boolean gifsicleEnable;
	private boolean ffmpegEnable;

	private final FileProperties fileProperties;

	MediaTool(FileProperties fileProperties) {
		this.fileProperties = fileProperties;
		detectServiceAvailable();
	}

	/**
	 * 判断是否是JPEG后缀
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isJPEG(String ext) {
		return JPEG.equalsIgnoreCase(ext) || JPG.equalsIgnoreCase(ext);
	}

	/**
	 * 判断是否是PNG后缀
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isPNG(String ext) {
		return PNG.equalsIgnoreCase(ext);
	}

	/**
	 * 判断是否是GIF后缀
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isGIF(String ext) {
		return GIF.equalsIgnoreCase(ext);
	}

	/**
	 * 判断是否是webp后缀
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isWEBP(String ext) {
		return WEBP.equalsIgnoreCase(ext);
	}

	/**
	 * 判断是否是可被处理的图片文件
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isProcessableImage(String ext) {
		return isJPEG(ext) || isGIF(ext) || isPNG(ext) || isWEBP(ext);
	}

	/**
	 * 判断是否是可被处理的视频文件
	 * 
	 * @param ext
	 * @return
	 */
	public static boolean isProcessableVideo(String ext) {
		return MP4.equalsIgnoreCase(ext) || MOV.equalsIgnoreCase(ext);
	}

	/**
	 * 是否支持目标格式的文件的处理
	 * <p>
	 * <b>仅支持：缩放以及读取图片|视频信息</b>
	 * </p>
	 * 
	 * @return
	 */
	public boolean canHandle(String ext) {
		if (MP4.equalsIgnoreCase(ext) || MOV.equalsIgnoreCase(ext))
			return ffmpegEnable;
		if (isGIF(ext))
			return gifsicleEnable && gmEnable;
		return gmEnable && (isJPEG(ext) || isPNG(ext) || isWEBP(ext));
	}

	/**
	 * 缩放一张图片
	 * 
	 * <p>
	 * <b>图片类型保持不变</b><br>
	 * <b>图片尺寸始终保持缩小或不变</b>
	 * </p>
	 * 
	 * @param resize
	 * @param src
	 * @param output
	 * @throws IOException
	 */
	public void resizeImage(Resize resize, Path src, Path output, boolean toWEBP) throws IOException {
		ImageInfo info = readImage(src);
		if (isGIF(info.getType()))
			resizeWithGifsicle(resize, src, output, toWEBP);
		else
			resizeWithGraphicsMagick(resize, src, output, toWEBP);
	}

	/**
	 * 读取图片信息
	 * 
	 * @param src
	 * @return 图片长宽和类型
	 * @throws IOException
	 */
	public ImageInfo readImage(Path src) throws IOException {
		try {
			List<String> commands = new ArrayList<>();
			commands.add(getGraphicsMagickPath());
			commands.add("identify");
			commands.add("-format");
			commands.add("%w-%h-%m");
			commands.add(src.toString());
			String rst = execCommands(commands);
			String[] array = rst.trim().split("-");
			// 0 is img width
			// 1 is img height
			// 2 is img type
			String type = array[2];
			if (type.startsWith("GIF")) {
				type = "GIF";
			}
			return new ImageInfo(Integer.parseInt(array[0]), Integer.parseInt(array[1]), type);
		} catch (Exception e) {
			try {
				return readGif(src);
			} catch (Exception ex) {
				throw new IOException(ex);
			}
		}
	}

	/**
	 * 获取视频封面图片
	 * <p>
	 * <b>目标为PNG格式图片</b>
	 * </p>
	 * 
	 * @param src
	 * @param output
	 * @throws IOException
	 */
	public void getVideoPoster(Path src, Path output) throws IOException {
		Path temp = Files.createTempFile(null, ".png");
		try {
			List<String> commands = Arrays.asList(getFfmpegPath("ffmpeg"), "-loglevel", "error", "-y", "-ss",
					"00:00:00", "-i", src.toString(), "-vframes", "1", "-q:v", "2", temp.toString());
			execCommands(commands);
			FileUtils.move(temp, output);
		} finally {
			FileUtils.deleteQuietly(temp);
		}

	}

	/**
	 * 读取视频信息尺寸已经时长信息
	 * 
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public VideoInfo readVideo(Path src) throws IOException {
		List<String> commands = Arrays.asList(getFfmpegPath("ffprobe"), "-v", "error", "-select_streams", "v:0",
				"-show_entries", "stream=width,height,duration", "-of", "default=noprint_wrappers=1:nokey=1",
				src.toString());
		String result = execCommands(commands);
		String[] lines = result.split(System.lineSeparator());
		if (lines.length != 3) {
			throw new IOException("获取视频信息失败:" + result);
		}
		int width = Integer.parseInt(lines[0]);
		int height = Integer.parseInt(lines[1]);
		try {
			return new VideoInfo(width, height, (int) Math.round(Double.parseDouble(lines[2])));
		} catch (NumberFormatException e) {
			List<String> commands2 = Arrays.asList(getFfmpegPath("ffprobe"), "-v", "error", "-show_entries",
					"format=duration", "-of", "default=noprint_wrappers=1:nokey=1", src.toString());
			String _result = execCommands(commands2);
			return new VideoInfo(width, height, (int) Math.round(Double.parseDouble(_result)));
		}
	}

	/**
	 * 将视频转化为指定尺寸的mp4
	 * 
	 * @param size
	 * @param src
	 * @param output
	 * @throws IOException
	 */
	public void toMP4(int height, int seconds, Path src, Path output) throws IOException {
		VideoInfo info = readVideo(src);
		List<String> cmdList = new ArrayList<>(
				Arrays.asList(getFfmpegPath("ffmpeg"), "-i", src.toString(), "-loglevel", "error", "-y"));
		if (info.getHeight() > height) {
			cmdList.add("-vf");
			cmdList.add("scale=-2:" + height + ":force_original_aspect_ratio=decrease");
		}
		if (seconds > 1) {
			cmdList.add("-t");
			cmdList.add(String.valueOf(seconds));
		}
		Path temp = Files.createTempFile(null, ".mp4");
		try {
			cmdList.addAll(Arrays.asList("-crf", String.valueOf(28), "-max_muxing_queue_size", "9999", "-c:v", "h264",
					"-c:a", "aac", "-map_metadata", "-1", temp.toString()));
			execCommands(cmdList);
			FileUtils.move(temp, output);
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

	private ImageInfo readGif(Path src) throws IOException {
		List<String> commands = List.of(getGifsiclePath(), "--conserve-memory", "--info", src.toString(), "#0");
		String info = execCommands(commands);
		String[] lines = info.split(System.lineSeparator());
		// first line can get frame count
		// second can get gif size
		String second = lines[1].trim();
		String[] sizes = second.split(" ")[2].split("x");
		return new ImageInfo(Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]), "GIF");
	}

	private void resizeWithGifsicle(Resize resize, Path src, Path output, boolean toWEBP) throws IOException {
		List<String> commands = new ArrayList<>(
				List.of(getGifsiclePath(), "--no-warnings", "--conserve-memory", "-careful"));

		// if it is webp format
		// we do not support animated webp
		// so we try to get first frame of gif and use gm convert it to webp
		if (toWEBP) {
			commands.add(src.toString());
			commands.add("#0");
			commands.add("-o");
			Path temp = Files.createTempFile(null, ".png");
			commands.add(temp.toString());
			try {
				execCommands(commands);
				resizeWithGraphicsMagick(resize, temp, output, true);
			} finally {
				FileUtils.deleteQuietly(temp);
			}
		}

		if (resize.getSize() != null) {
			int size = resize.getSize();
			commands.add("--resize-fit");
			commands.add(size + "x" + size);
		} else {
			if (!resize.isKeepRatio()) {
				commands.add("--resize");
				commands.add(resize.getWidth() + "x" + resize.getHeight());
			} else {
				if (resize.getWidth() == null) {
					commands.add("--resize-height");
					commands.add(String.valueOf(resize.getHeight()));
				} else if (resize.getHeight() == null) {
					commands.add("--resize-width");
					commands.add(String.valueOf(resize.getWidth()));
				} else {
					commands.add("--resize-fit");
					commands.add(resize.getWidth() + "x" + resize.getHeight());
				}
			}
		}

		Path temp = Files.createTempFile(null, ".gif");
		try {
			commands.addAll(List.of(src.toString(), "-o", temp.toString()));
			execCommands(commands);

			synchronized (this) {
				FileUtils.move(temp, output);
			}
		} finally {
			FileUtils.deleteQuietly(temp);
		}

	}

	// native support webp
	// no need to covert
	private void resizeWithGraphicsMagick(Resize resize, Path src, Path output, boolean toWEBP) throws IOException {
		List<String> commands = new ArrayList<>();
		commands.add(getGraphicsMagickPath());
		commands.add("convert");
		commands.add(src.toString());
		commands.add("-strip");// 移除meta信息
		commands.add("-thumbnail");// 制作缩略图

		String resizeCommand;
		if (resize.getSize() != null) {
			resizeCommand = resize.getSize() + "x" + resize.getSize() + ">";
		} else {
			if (!resize.isKeepRatio()) {
				resizeCommand = resize.getWidth() + "x" + resize.getHeight() + "!";
			} else {
				if (resize.getWidth() == null) {
					resizeCommand = Integer.MAX_VALUE + "x" + resize.getHeight() + ">";
				} else if (resize.getHeight() == null) {
					resizeCommand = resize.getWidth() + "x" + Integer.MAX_VALUE + ">";
				} else {
					resizeCommand = resize.getWidth() + "x" + resize.getHeight() + ">";
				}
			}
		}
		commands.add(resizeCommand);

		String ext = FileUtils.getFileExtension(src);
		// set white background
		if (!isPNG(ext) && !isWEBP(ext)) {
			commands.add("-background");
			commands.add("rgb(255,255,255)");
			commands.add("-extent");
			commands.add("0x0");
			commands.add("+matte");
		}

		commands.add("-auto-orient");

		if (resize.getQuality() != null) {
			commands.add("-quality");
			commands.add(String.valueOf(resize.getQuality()));
		}

		Path temp = Files.createTempFile(null, "." + ext);
		try {
			// write to temp file
			commands.add(temp.toString());
			execCommands(commands);

			if (isPNG(ext) && pngQuantEnable) {
				// compress png use pngquant
				execCommands(Arrays.asList(getPngQuantPath(), "-f", "-o", temp.toString(), temp.toString()));
			}

			synchronized (this) {
				FileUtils.move(temp, output);
			}

		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

	private String execCommands(List<String> commands) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		try (InputStream is = process.getInputStream()) {
			String msg = StreamUtils.toString(is);
			try {
				if (process.waitFor() == 0)
					return msg;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			throw new IOException(commands + " exec error :" + msg);
		}
	}

	private String getGifsiclePath() throws IOException {
		return fileProperties.getGifsiclePath() != null
				? new File(fileProperties.getGifsiclePath(), "gifsicle").getCanonicalPath()
				: "gifsicle";
	}

	private String getGraphicsMagickPath() throws IOException {
		return fileProperties.getGraphicsMagickPath() != null
				? new File(fileProperties.getGraphicsMagickPath(), "gm").getCanonicalPath()
				: "gm";
	}

	private String getFfmpegPath(String name) throws IOException {
		return fileProperties.getFfmpegPath() != null
				? new File(fileProperties.getFfmpegPath(), name).getCanonicalPath()
				: name;
	}

	private String getPngQuantPath() throws IOException {
		return fileProperties.getPngQuantPath() != null
				? new File(fileProperties.getPngQuantPath(), "pngquant").getCanonicalPath()
				: "pngquant";
	}

	private void detectServiceAvailable() {
		try {
			execCommands(Arrays.asList(getGraphicsMagickPath(), "-version"));
			gmEnable = true;
		} catch (Exception e) {
			gmEnable = false;
		}

		try {
			execCommands(Arrays.asList(getPngQuantPath(), "--version"));
			pngQuantEnable = true;
		} catch (Exception e) {
			pngQuantEnable = false;
		}

		try {
			execCommands(Arrays.asList(getGifsiclePath(), "--version"));
			gifsicleEnable = true;
		} catch (Exception e) {
			gifsicleEnable = false;
		}

		try {
			execCommands(Arrays.asList(getFfmpegPath("ffmpeg"), "-version"));
			ffmpegEnable = true;
		} catch (Exception e) {
			ffmpegEnable = false;
		}
	}

}
