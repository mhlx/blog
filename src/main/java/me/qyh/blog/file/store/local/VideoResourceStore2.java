package me.qyh.blog.file.store.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.entity.CommonFile;

/**
 * @since 7.0
 * @author wwwqyhme
 *
 */
public class VideoResourceStore2 extends VideoResourceStore implements ApplicationListener<ContextClosedEvent> {

	@Autowired
	private Messages messages;

	private final ExecutorService es = Executors.newSingleThreadExecutor();

	private final Map<String, Boolean> processing = new ConcurrentHashMap<>();

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
			VideoInfo info = getVideoInfo(dest);
			compress(info, dest, key);
			synchronized (this) {
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
		} else {
			VideoInfo vi;
			try {
				vi = getVideoInfo(path);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			compress(vi, path, key);
			return Optional.of(getProcessingResource());
		}
	}

	protected Resource getProcessingResource() {
		return new StringResource(messages.getMessage("video.compressing", "视频正在压缩中"));
	}

	@Override
	public void setMaxSize(Integer maxSize) {
		throw new SystemException("请使用构造函数设置最大尺寸");
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		if (!es.isShutdown())
			es.shutdownNow();
	}

	private void compress(VideoInfo info, Path dest, String key) {
		es.execute(() -> {
			Path compress = getCompress(key, dest);
			if (FileUtils.exists(compress)) {
				return;
			}
			if (processing.putIfAbsent(key, Boolean.TRUE) == null) {
				try {
					compress(info, dest, compress);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					processing.remove(key);
				}
			}
		});
	}

	@Override
	protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
		if (resource instanceof StringResource) {
			return MediaType.valueOf("text/html;charset=UTF-8");
		}
		return super.getMediaType(request, resource);
	}

	private final class StringResource extends AbstractResource {

		private final String content;

		private StringResource(String content) {
			super();
			this.content = content;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(content.getBytes(Constants.CHARSET));
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public long lastModified() throws IOException {
			return -1;
		}

	}

}
