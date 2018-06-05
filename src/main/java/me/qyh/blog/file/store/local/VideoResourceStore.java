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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
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
		VideoSize size;
		try {
			synchronized (this) {
				if (needCompress()) {
					compress(getVideoSize(dest), dest);
				}
				size = getVideoSize(dest);
				extraPoster(dest, getPoster(key));
			}
		} catch (Exception e) {
			FileUtils.deleteQuietly(dest);
			logger.warn(e.getMessage(), e);
			throw new LogicException("video.corrupt", "不是正确的视频文件或者视频已经损坏");
		}
		file.setSize(FileUtils.getSize(dest));
		file.setWidth(size.width);
		file.setHeight(size.height);
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

	protected void compress(VideoSize size, Path original) throws Exception {
		Path temp = FileUtils.appTemp(FileUtils.getFileExtension(original));
		List<String> cmdList = new ArrayList<>(
				Arrays.asList("ffmpeg", "-i", original.toString(), "-loglevel", "error", "-y"));
		if (size.width > maxSize || size.height > maxSize) {
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

	protected class VideoSize {
		private final int width;
		private final int height;

		public VideoSize(int width, int height) {
			super();
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

	}

	private VideoSize getVideoSize(Path video) throws ProcessException {
		String[] cmdArray = new String[] { "ffprobe", "-v", "error", "-show_entries", "stream=width,height", "-of",
				"default=noprint_wrappers=1", video.toString() };
		String result = ProcessUtils.runProcess(cmdArray, 10, TimeUnit.SECONDS)
				.orElseThrow(() -> new SystemException("没有返回预期的尺寸信息"));
		String[] sizes = result.split(System.lineSeparator());
		return new VideoSize(Integer.parseInt(sizes[0].split("=")[1]), Integer.parseInt(sizes[1].split("=")[1]));
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

}
