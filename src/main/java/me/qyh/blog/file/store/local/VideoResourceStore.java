/*
 * Copyright 2018 qyh.me
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Jsons.ExpressionExecutor;
import me.qyh.blog.core.util.Jsons.ExpressionExecutors;
import me.qyh.blog.file.entity.CommonFile;
import me.qyh.blog.file.store.ProcessUtils;

/**
 * FFMPEG 4.0+
 * 
 * @author wwwqyhme
 *
 */
public class VideoResourceStore extends ThumbnailSupport {

	private final String[] allowExtensions;

	private Integer maxSize;// 视频最大尺寸
	private final int timeoutSecond;
	private static final int DEFAULT_CRF = 24;
	private Integer crf = DEFAULT_CRF;

	public VideoResourceStore(String urlPatternPrefix, String[] allowExtensions, int timeoutSecond) {
		super(urlPatternPrefix);
		this.allowExtensions = allowExtensions;
		this.timeoutSecond = timeoutSecond;
	}

	public VideoResourceStore() {
		this("video", new String[] { "mp4", "mov" }, 60);
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
					compress(getVideoSize(dest), dest);
				}
				getVideoSize(dest);
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
	protected Optional<Resource> handleOriginalFile(Path path, HttpServletRequest request) {
		return Optional.of(new PathResource(path));
	}

	@Override
	protected void extraPoster(Path original, Path poster) throws Exception {
		Path temp = FileUtils.appTemp(FileUtils.getFileExtension(poster));
		String[] cmdArray = new String[] { "ffmpeg", "-loglevel", "error", "-y", "-ss", "00:00:00", "-i",
				original.toString(), "-vframes", "1", "-q:v", "2", temp.toString() };
		ProcessUtils.runProcess(cmdArray, timeoutSecond, TimeUnit.SECONDS);
		FileUtils.move(temp, poster);
	}

	@Override
	public Map<Message, String> getProperties(String key) {
		Optional<Path> op = super.getFile(key);
		if (op.isPresent()) {
			try {
				VideoInfo info = this.getVideoSize(op.get());
				return Map.of(new Message("video.width", "视频宽度"), info.width, new Message("video.height", "视频高度"),
						info.height, new Message("video.duration", "视频长度"), info.duration);
			} catch (ProcessException e) {
				return Map.of();
			}

		}
		return super.getProperties(key);
	}

	protected void compress(VideoInfo info, Path original) throws Exception {
		Path temp = FileUtils.appTemp(FileUtils.getFileExtension(original));
		List<String> cmdList = new ArrayList<>(
				Arrays.asList("ffmpeg", "-i", original.toString(), "-loglevel", "error", "-y"));
		if (Integer.parseInt(info.width) > maxSize || Integer.parseInt(info.height) > maxSize) {
			cmdList.add("-vf");
			cmdList.add("scale=w=" + maxSize + ":h=" + maxSize + ":force_original_aspect_ratio=decrease");
		}
		cmdList.addAll(Arrays.asList("-crf", String.valueOf(crf), "-max_muxing_queue_size", "9999", "-vcodec", "h264",
				"-acodec", "aac", temp.toString()));
		ProcessUtils.runProcess(cmdList, timeoutSecond, TimeUnit.SECONDS);
		if (!FileUtils.deleteQuietly(original)) {
			throw new SystemException("删除原文件失败");
		}
		FileUtils.move(temp, original);
	}

	protected class VideoInfo {
		private final String width;
		private final String height;
		private final String duration;

		public VideoInfo(String width, String height, String duration) {
			super();
			this.duration = duration;
			this.width = width;
			this.height = height;
		}

		public String getDuration() {
			return duration;
		}

		public String getWidth() {
			return width;
		}

		public String getHeight() {
			return height;
		}
	}

	private VideoInfo getVideoSize(Path video) throws ProcessException {
		String[] cmdArray = new String[] { "ffprobe", "-v", "error", "-print_format", "json", "-show_streams",
				video.toString() };
		String result = ProcessUtils.runProcess(cmdArray, 10, TimeUnit.SECONDS)
				.orElseThrow(() -> new SystemException("没有返回预期的尺寸信息"));
		ExpressionExecutors executors = Jsons.readJson(result).executeForExecutors("streams");
		for (ExpressionExecutor executor : executors) {
			if (executor.execute("codec_type").get().equals("video")) {
				String width = executor.execute("width").get();
				String height = executor.execute("height").get();
				String duration = formatDuration(Double.parseDouble(executor.execute("duration").get()));
				return new VideoInfo(width, height, duration);
			}
		}
		throw new SystemException("无法获取视频信息:" + result);
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public void setCrf(Integer crf) {
		this.crf = Objects.requireNonNull(crf);
	}

	private boolean needCompress() {
		return maxSize != null;
	}

	private static String formatDuration(double duration) {
		int h = -1;
		int m = -1;
		int s = 0;
		double rst = duration;
		if (rst > 3600) {
			h = (int) Math.floor(rst / 3600);
			rst = rst - h * 3600;
		}
		if (rst > 60) {
			m = (int) Math.floor(rst / 60);
			rst = rst - m * 60;
		}
		s = (int) Math.rint(rst);
		StringBuilder sb = new StringBuilder();
		if (h > -1) {
			sb.append(h).append("h");
		}
		if (m > -1) {
			sb.append(m).append("m");
		}
		sb.append(s).append("s");
		return sb.toString();
	}

}
