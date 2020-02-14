package me.qyh.blog.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.exception.LogicException;
import me.qyh.blog.file.FileService.SecurityType;
import me.qyh.blog.utils.FileUtils;

@Controller
@RequestMapping("console")
@Conditional(FileCondition.class)
@Validated
public class FileController {

	private final FileService fileService;

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@GetMapping("files")
	public String index() {
		return "console/file/index";
	}

	@GetMapping("file/edit")
	public String edit(@Path(message = "非法的路径") @RequestParam("path") String path, Model model) {
		FileInfoDetail fid = fileService.getFileInfoDetail(path);
		if (!fid.isEditable()) {
			throw new LogicException("file.canNoEdit", "文件:" + path + "无法被编辑", path);
		}
		String ext = fid.getExt().toLowerCase();
		if ("htm".equals(ext)) {
			ext = "html";
		}
		model.addAttribute("file", fid);
		return "console/file/edit_" + ext;
	}

	@ResponseBody
	@GetMapping("files/query")
	public FilePageResult query(@Valid FileQueryParam param) {
		if (!param.hasPageSize()) {
			param.setPageSize(10);
		}
		return fileService.query(param);
	}

	@ResponseBody
	@GetMapping(value = "file/get")
	public FileInfoDetail getFileInfo(@Path(message = "非法的路径") @RequestParam String path) {
		return fileService.getFileInfoDetail(path);
	}

	@ResponseBody
	@PostMapping(value = "file/delete")
	public void deleteFile(@Path(message = "非法的路径") @RequestParam String path) {
		fileService.delete(path);
	}

	@ResponseBody
	@PostMapping(value = "file/update")
	public void update(@Path(message = "非法的路径") @RequestParam String path, @Valid @RequestBody FileUpdate update) {
		fileService.updateFile(path, update);
	}

	@ResponseBody
	@PostMapping(value = "file/create")
	public FileInfoDetail createFile(@Valid @RequestBody FileCreate create) {
		FileInfoDetail fid = fileService.createFile(create);
		return fid;
	}

	@ResponseBody
	@PostMapping(value = "file/upload")
	public FileInfoDetail createFile(@Path(message = "非法的文件夹路径") @RequestParam("dirPath") String dirPath,
			@RequestParam("file") MultipartFile file) {
		FileInfoDetail fid = fileService.save(dirPath, new MultipartFileReadablePath(file));
		return fid;
	}

	@ResponseBody
	@PostMapping(value = "file/copy")
	public FileInfoDetail copy(@Path(message = "非法的原文件路径") @RequestParam("source") String source,
			@Path(message = "非法的目标文件路径") @RequestParam("dir") String dest) {
		FileInfoDetail fid = fileService.copy(source, dest);
		return fid;
	}

	@ResponseBody
	@PostMapping(value = "securityPath/save")
	public void makeProtect(@Path(message = "非法的文件路径") String path,
			@Size(max = 20, message = "密码长度不能超过20个字符") @RequestParam String password) {
		fileService.makePathSecurity(path, password);
	}

	@ResponseBody
	@PostMapping(value = "securityPath/delete")
	public void deleteSecurityPath(@RequestParam String path) {
		fileService.deleteSecurityPath(path);
	}

	@ResponseBody
	@GetMapping("securityPaths")
	public Map<SecurityType, List<String>> securityPaths() {
		return fileService.getSecurityPaths();
	}

	private class MultipartFileReadablePath implements ReadablePath {
		private final MultipartFile multipartFile;

		public MultipartFileReadablePath(MultipartFile multipartFile) {
			super();
			this.multipartFile = multipartFile;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return multipartFile.getInputStream();
		}

		@Override
		public String fileName() {
			return multipartFile.getOriginalFilename();
		}

		@Override
		public long size() {
			return multipartFile.getSize();
		}

		@Override
		public long lastModified() {
			return -1;
		}

		@Override
		public String getExtension() {
			return FileUtils.getFileExtension(multipartFile.getOriginalFilename());
		}

	}

}
