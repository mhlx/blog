package me.qyh.blog.file.store.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.ProcessUtils;

/**
 * FFMPEG 4.0+
 * <p>
 * <b>use {@code VideoResourceStore2} instead of</b>
 * </p>
 * 
 * @author wwwqyhme
 *
 */
public class VideoResourceStore extends ThumbnailSupport {

	private final String[] allowExtensions;

	private Integer maxSize;// 视频最大尺寸
	private final int timeoutSecond;
	private static final int DEFAULT_CRF = 30;
	private Integer crf = DEFAULT_CRF;

	public VideoResourceStore(String urlPatternPrefix, String[] allowExtensions, int timeoutSecond) {
		super(urlPatternPrefix);
		this.allowExtensions = allowExtensions;
		this.timeoutSecond = timeoutSecond;
	}

	public VideoResourceStore(String urlPatternPrefix) {
		this(urlPatternPrefix, new String[] { "mp4", "mov" }, 120);
	}

	public VideoResourceStore() {
		this("video");
	}

	@Override
	public MultipartFile preHandler(MultipartFile file) throws LogicException {
		return new MultipartFile() {

			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {
				file.transferTo(dest);
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
			public String getOriginalFilename() {
				if (needCompress()) {
					String originalFilename = file.getOriginalFilename();
					String name = FileUtils.getNameWithoutExtension(originalFilename);
					return name + ".mp4";
				} else {
					return file.getOriginalFilename();
				}
			}

			@Override
			public String getName() {
				return file.getName();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return file.getInputStream();
			}

			@Override
			public String getContentType() {
				return file.getContentType();
			}

			@Override
			public byte[] getBytes() throws IOException {
				return file.getBytes();
			}
		};
	}

	@Override
	protected CommonFile doStore(Path dest, String key, MultipartFile mf) throws LogicException {
		CommonFile file = super.doStore(dest, key, mf);
		try {
			synchronized (this) {
				if (needCompress()) {
					// 怎么处理耗时过长的问题？？
					compress(getVideoInfo(dest), dest, dest);
				}
				getVideoInfo(dest);
				extraPoster(dest, getPoster(key));
			}
		} catch (Exception e) {
			FileUtils.deleteQuietly(dest);
			logger.warn(e.getMessage(), e);
			throw new LogicException("video.corrupt", "不是正确的视频文件或者视频已经损坏");
		}
		file.setSize(FileUtils.getSize(dest));
		return file;
	}

	@Override
	public boolean canStore(MultipartFile multipartFile) {
		String ext = FileUtils.getFileExtension(multipartFile.getOriginalFilename());
		return !ext.isEmpty() && Arrays.stream(allowExtensions).anyMatch(ext::equalsIgnoreCase);
	}

	@Override
	protected Optional<Resource> handleOriginalFile(String key, Path path, HttpServletRequest request) {
		return Optional.of(new FileSystemResource(path));
	}

	@Override
	protected void extraPoster(Path original, Path poster) throws Exception {
		Path temp = FileUtils.appTemp(FileUtils.getFileExtension(poster));
		String[] cmdArray = new String[] { "ffmpeg", "-loglevel", "error", "-y", "-ss", "00:00:00", "-i",
				original.toString(), "-vframes", "1", "-q:v", "2", temp.toString() };
		ProcessUtils.runProcess(cmdArray, timeoutSecond, TimeUnit.SECONDS, false);
		FileUtils.move(temp, poster);
	}

	@Override
	public Map<Message, String> getProperties(String key) {
		Optional<Path> op = super.getFile(key);
		if (op.isPresent()) {
			try {
				VideoInfo info = this.getVideoInfo(op.get());
				return Map.of(new Message("video.width", "视频宽度"), String.valueOf(info.width),
						new Message("video.height", "视频高度"), String.valueOf(info.height),
						new Message("video.duration", "视频长度"), String.valueOf(info.duration) + "s");
			} catch (IOException e) {
				return Map.of();
			}

		}
		return super.getProperties(key);
	}

	protected void compress(VideoInfo info, Path original, Path dest) throws Exception {
		Path temp = FileUtils.appTemp(FileUtils.getFileExtension(original));
		List<String> cmdList = new ArrayList<>(
				Arrays.asList("ffmpeg", "-i", original.toString(), "-loglevel", "error", "-y"));
		if (info.width > maxSize || info.height > maxSize) {
			cmdList.add("-vf");
			cmdList.add("scale=w=" + maxSize + ":h=" + maxSize + ":force_original_aspect_ratio=decrease");
		}
		cmdList.addAll(Arrays.asList("-crf", String.valueOf(crf), "-max_muxing_queue_size", "9999", "-c:v", "h264",
				"-c:a", "aac", "-map_metadata", "-1", temp.toString()));
		ProcessUtils.runProcess(cmdList, timeoutSecond, TimeUnit.SECONDS, false);
		FileUtils.move(temp, dest);
	}

	protected class VideoInfo {
		private final int width;
		private final int height;
		private final int duration;

		public VideoInfo(int width, int height, int duration) {
			super();
			this.width = width;
			this.height = height;
			this.duration = duration;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getDuration() {
			return duration;
		}

	}

	protected VideoInfo getVideoInfo(Path video) throws IOException {
		// https://trac.ffmpeg.org/wiki/FFprobeTips
		String[] cmdArray = new String[] { "ffprobe", "-v", "error", "-select_streams", "v:0", "-show_entries",
				"stream=width,height,duration", "-of", "default=noprint_wrappers=1:nokey=1", video.toString() };
		String result = ProcessUtils.runProcess(cmdArray, timeoutSecond, TimeUnit.SECONDS, false);
		String[] lines = result.split(System.lineSeparator());
		if (lines.length != 3) {
			throw new SystemException("获取视频信息失败:" + result);
		}
		int width = Integer.parseInt(lines[0]);
		int height = Integer.parseInt(lines[1]);
		try {
			return new VideoInfo(width, height, (int) Math.round(Double.parseDouble(lines[2])));
		} catch (NumberFormatException e) {
			String[] _cmdArray = new String[] { "ffprobe", "-v", "error", "-show_entries", "format=duration", "-of",
					"default=noprint_wrappers=1:nokey=1", video.toString() };
			String _result = ProcessUtils.runProcess(_cmdArray, timeoutSecond, TimeUnit.SECONDS, false);
			return new VideoInfo(width, height, (int) Math.round(Double.parseDouble(_result)));
		}
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public void setCrf(Integer crf) {
		this.crf = Objects.requireNonNull(crf);
	}

	protected final boolean needCompress() {
		return maxSize != null;
	}

	@Override
	public int getOrder(MultipartFile file) {
		if (canStore(file)) {
			return Integer.MAX_VALUE;
		}
		return Integer.MIN_VALUE;
	}
}
