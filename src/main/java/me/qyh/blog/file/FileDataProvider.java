package me.qyh.blog.file;

import java.util.Map;

import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.web.template.tag.DataProviderSupport;

public class FileDataProvider extends DataProviderSupport<FileInfoDetail> {

	private final FileService fileService;

	public FileDataProvider(FileService fileService) {
		super("file");
		this.fileService = fileService;
	}

	@Override
	public FileInfoDetail provide(Map<String, String> attributesMap) throws Exception {
		String path = attributesMap.get("path");
		if (path == null) {
			throw new ResourceNotFoundException("file.notExists", "文件不存在");
		}
		return fileService.getFileInfoDetail(path);
	}

}
