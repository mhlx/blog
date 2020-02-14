package me.qyh.blog.file;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import me.qyh.blog.utils.FileUtils;

class FileResourceResolver implements ResourceResolver {

	private static final String WEBP_ACCEPT = "image/webp";
	private static final String CONCAT = "X";
	private static final String FORCE = "!";

	private static final Optional<Resize> INVALID_RESIZE = Optional.of(new Resize(-1));

	private final FileService fileService;
	private final FileProperties fileProperties;

	public FileResourceResolver(FileService fileService, FileProperties fileProperties) {
		this.fileService = fileService;
		this.fileProperties = fileProperties;
	}

	@Override
	public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations,
			ResourceResolverChain chain) {
		Optional<Resize> opResize = getResizeFromPath(requestPath);
		if (opResize == INVALID_RESIZE)
			return null;
		String sourcePath = opResize.isEmpty() ? requestPath : getSourcePathByResizePath(requestPath);
		String ext = FileUtils.getFileExtension(sourcePath);
		if (opResize.isPresent()) {
			Resize resize = opResize.get();
			if (resize.isInvalid())
				return null;
			String accept = request.getHeader("Accept");
			boolean toWEBP = (MediaTool.isJPEG(ext) || MediaTool.isPNG(ext)) && accept != null
					&& accept.contains(WEBP_ACCEPT);
			return fileService.getThumbnail(sourcePath, resize, toWEBP).map(ReadablePathSource::new).orElse(null);
		} else {
			if (MediaTool.isProcessableImage(FileUtils.getFileExtension(sourcePath))
					&& fileProperties.isSourceProtect()) {
				return null;
			}
			return fileService.getProcessedFile(sourcePath).map(ReadablePathSource::new).orElse(null);
		}
	}

	@Override
	public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
		return null;
	}

	/**
	 * <ul>
	 * <li>123.png=>empty</li>
	 * <li>123.png/200=>new Resize(200)</li>
	 * <li>123.png/200x300=>new Resize(200,300,true)</li>
	 * <li>123.png/200x300!=>new Resize(200,300,false)</li>
	 * <li>123.png/x300=>new Resize(null,300,true)</li>
	 * <li>123.png/200x=>new Resize(200,null,true)</li>
	 * </ul>
	 * 
	 * @param path
	 * @return
	 */
	protected Optional<Resize> getResizeFromPath(String path) {

		String ext = FileUtils.getFileExtension(path);
		if (!ext.strip().isEmpty())
			return Optional.empty();

		String name = FileUtils.getNameWithoutExtension(path);
		if (name.indexOf(CONCAT) == -1)
			try {
				return Optional.of(new Resize(Integer.parseInt(name)));
			} catch (NumberFormatException e) {
				return INVALID_RESIZE;
			}
		boolean keepRatio = name.endsWith(FORCE);
		String sizeInfo = keepRatio ? name.substring(0, name.length() - 1) : name;

		// only height
		if (sizeInfo.startsWith(CONCAT))
			try {
				return Optional.of(new Resize(null, Integer.parseInt(sizeInfo.substring(1)), true));
			} catch (NumberFormatException e) {
				return INVALID_RESIZE;
			}
		// only width
		if (sizeInfo.endsWith(CONCAT))
			try {
				return Optional
						.of(new Resize(null, Integer.parseInt(sizeInfo.substring(0, sizeInfo.length() - 1)), true));
			} catch (NumberFormatException e) {
				return INVALID_RESIZE;
			}
		// both
		String[] array = sizeInfo.split(CONCAT);
		if (array.length != 2)
			return INVALID_RESIZE;
		try {
			return Optional.of(new Resize(Integer.parseInt(array[0]), Integer.parseInt(array[1]), keepRatio));
		} catch (NumberFormatException e) {
			return INVALID_RESIZE;
		}
	}

	private String getSourcePathByResizePath(String path) {
		String sourcePath = path;
		int index = path.lastIndexOf('/');
		if (index != -1) {
			sourcePath = path.substring(0, index);
		}
		return sourcePath;
	}
}
