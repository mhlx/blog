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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.entity.CommonFile;

/**
 * @since 7.0
 * @author wwwqyhme
 *
 */
public class VideoResourceStore2 extends VideoResourceStore {

	public VideoResourceStore2(String urlPatternPrefix, String[] allowExtensions, int timeoutSecond, int maxSize) {
		super(urlPatternPrefix, allowExtensions, timeoutSecond);
		super.setMaxSize(maxSize);
	}

	public VideoResourceStore2(int maxSize) {
		super("video2");
		super.setMaxSize(maxSize);
	}

	public VideoResourceStore2(String urlPatternPrefix, int maxSize) {
		super(urlPatternPrefix);
		super.setMaxSize(maxSize);
	}

	@Override
	public MultipartFile preHandler(MultipartFile file) throws LogicException {
		return file;
	}

	@Override
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
		try {
			synchronized (this) {
				compress(getVideoInfo(dest), dest, getCompress(key, dest));
				extraPoster(dest, getPoster(key));
			}
		} catch (Exception e) {
			FileUtils.deleteQuietly(dest);
			logger.warn(e.getMessage(), e);
			throw new LogicException("video.corrupt", "不是正确的视频文件或者视频已经损坏");
		}
		cf.setSize(FileUtils.getSize(dest));
		return cf;
	}

	protected Path getCompress(String key, Path original) {
		return getPoster(key).getParent().resolve(original.getFileName().toString() + ".compress.mp4");
	}

	@Override
	protected Optional<Resource> handleOriginalFile(String key, Path path, HttpServletRequest request) {
		if (!FileUtils.exists(path)) {
			return Optional.empty();
		}
		Path compress = getCompress(key, path);
		if (FileUtils.exists(compress)) {
			return Optional.of(new FileSystemResource(compress));
		}
		synchronized (this) {
			if (FileUtils.exists(compress)) {
				return Optional.of(new FileSystemResource(compress));
			}
			try {
				compress(getVideoInfo(path), path, compress);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
				return Optional.empty();
			}
		}
		return Optional.of(new FileSystemResource(compress));
	}

	@Override
	public void setMaxSize(Integer maxSize) {
		throw new SystemException("请使用构造函数设置最大尺寸");
	}

}
