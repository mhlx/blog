package me.qyh.blog.file;

import java.util.Map;

import org.springframework.validation.BindException;

import me.qyh.blog.web.template.tag.DataProviderSupport;

public class FilesDataProvider extends DataProviderSupport<FilePageResult> {

	private final FileService fileService;

	public FilesDataProvider(FileService fileService) {
		super("filePage");
		this.fileService = fileService;
	}

	@Override
	public FilePageResult provide(Map<String, String> attributesMap) throws Exception {
		FileQueryParam param = bindQueryParam(attributesMap);
		return fileService.query(param);
	}

	private FileQueryParam bindQueryParam(Map<String, String> attributesMap) throws BindException {
		String name = attributesMap.remove("_name");
		if (name != null) {
			attributesMap.put("name", name);
		}
		return bind(new FileQueryParam(), attributesMap);
	}
}
