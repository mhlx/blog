package me.qyh.blog.file;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.utils.FileUtils;

@Authenticated
@RestController
@RequestMapping("api")
@Conditional(FileCondition.class)
@Validated
public class FileController {

	private final FileService fileService;
	private final BlogProperties blogProperties;

	public FileController(FileService fileService, BlogProperties blogProperties) {
		super();
		this.fileService = fileService;
		this.blogProperties = blogProperties;
	}

	@GetMapping("file")
	public FileInfoDetail getFile(@Path(message = "非法的路径") @RequestParam("path") String path, Model model) {
		return fileService.getFileInfoDetail(path)
				.orElseThrow(() -> new ResourceNotFoundException("file.notExists", "文件不存在"));
	}

	@GetMapping("file/statistic")
	public FileStatistic getFileStatistic() {
		return fileService.getFileStatistic();
	}

	@GetMapping("files")
	public FilePageResult query(@Valid FileQueryParam param) {
		return fileService.query(param);
	}

	@DeleteMapping(value = "file")
	public ResponseEntity<?> deleteFile(@Path(message = "非法的路径") @RequestParam("path") String path) {
		fileService.delete(path);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "file")
	public ResponseEntity<?> update(@Path(message = "非法的路径") @RequestParam("path") String path,
			@Valid @RequestBody FileUpdate update) {
		fileService.updateFile(path, update);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "file")
	public ResponseEntity<FileInfoDetail> createFile(@Valid @RequestBody FileCreate create) {
		FileInfoDetail detail = fileService.createFile(create);
		return ResponseEntity.created(blogProperties.buildUrl("api/file?path=" + detail.getPath())).body(detail);
	}

	@PostMapping(value = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileInfoDetail> uploadFile(
			@Path(message = "非法的文件夹路径") @RequestParam("dirPath") String dirPath,
			@RequestParam("file") MultipartFile file) {
		FileInfoDetail detail = fileService.save(dirPath, new MultipartFileReadablePath(file));
		return ResponseEntity.created(blogProperties.buildUrl("api/file?path=" + detail.getPath())).body(detail);
	}

	@PostMapping(value = "file", params = { "path", "dir" })
	public ResponseEntity<FileInfoDetail> copy(@Path(message = "非法的原文件路径") @RequestParam("path") String path,
			@Path(message = "非法的目标文件路径") @RequestParam("dir") String dest) {
		FileInfoDetail detail = fileService.copy(path, dest);
		return ResponseEntity.created(blogProperties.buildUrl("api/file?path=" + detail.getPath())).body(detail);
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
