/*
 * Copyright 2016 qyh.me
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
package me.qyh.blog.web.controller.console.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.entity.BlogFile;
import me.qyh.blog.file.entity.BlogFile.BlogFileType;
import me.qyh.blog.file.service.FileService;
import me.qyh.blog.file.store.FileStore;
import me.qyh.blog.file.validator.Base64FileUploadValidator;
import me.qyh.blog.file.validator.BlogFileQueryParamValidator;
import me.qyh.blog.file.validator.BlogFileUploadValidator;
import me.qyh.blog.file.vo.Base64FileUpload;
import me.qyh.blog.file.vo.BlogFilePageResult;
import me.qyh.blog.file.vo.BlogFileProperties;
import me.qyh.blog.file.vo.BlogFileQueryParam;
import me.qyh.blog.file.vo.BlogFileUpload;
import me.qyh.blog.file.vo.FileStoreBean;
import me.qyh.blog.file.vo.UploadedFile;
import me.qyh.blog.template.vo.Base64MultipareFile;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class FileConsole extends BaseMgrController {

	@Autowired
	private FileService fileService;
	@Autowired
	private BlogFileQueryParamValidator blogFileParamValidator;
	@Autowired
	private BlogFileUploadValidator blogFileUploadValidator;
	@Autowired
	private Base64FileUploadValidator base64FileUploadValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "blogFileQueryParam")
	protected void initBlogFileQueryParamBinder(WebDataBinder binder) {
		binder.setValidator(blogFileParamValidator);
	}

	@InitBinder(value = "blogFileUpload")
	protected void initBlogUploadBinder(WebDataBinder binder) {
		binder.setValidator(blogFileUploadValidator);
	}

	@InitBinder(value = "base64FileUpload")
	protected void initBase64FileUploadBinder(WebDataBinder binder) {
		binder.setValidator(base64FileUploadValidator);
	}

	@GetMapping("stores")
	public List<FileStoreBean> allStores() {
		List<FileStore> stores = fileService.allStorableStores();
		return stores.stream().map(FileStoreBean::new).collect(Collectors.toList());
	}

	@GetMapping("files")
	public BlogFilePageResult query(@Validated BlogFileQueryParam blogFileQueryParam) throws LogicException {
		blogFileQueryParam.setIgnorePaging(false);
		blogFileQueryParam.setExtensions(Set.of());
		blogFileQueryParam.setPageSize(configServer.getGlobalConfig().getFilePageSize());
		return fileService.queryBlogFiles(blogFileQueryParam);
	}

	@PostMapping(value = "store/{id}/files", params = { "base64Upload" })
	public ResponseEntity<List<UploadedFile>> uploadWithBase64(@Validated Base64FileUpload base64FileUpload,
			@PathVariable("id") Integer id, BindingResult result) throws LogicException {
		Optional<Message> validateError = Webs.getFirstError(result);
		if (validateError.isPresent()) {
			throw new LogicException(validateError.get());
		}
		base64FileUpload.setStore(id);
		String newName = rename(base64FileUpload);
		Base64MultipareFile file = new Base64MultipareFile(newName, base64FileUpload.getBase64());
		BlogFileUpload upload = new BlogFileUpload();
		upload.setFiles(List.of(file));
		upload.setParent(base64FileUpload.getParent());
		upload.setStore(base64FileUpload.getStore());

		List<UploadedFile> uploadedFiles = fileService.upload(upload);
		return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
	}

	@PostMapping("store/{id}/files")
	public ResponseEntity<List<UploadedFile>> upload(@Validated BlogFileUpload blogFileUpload,
			@PathVariable("id") Integer id, BindingResult result) throws LogicException {
		Optional<Message> validateError = Webs.getFirstError(result);
		if (validateError.isPresent()) {
			throw new LogicException(validateError.get());
		}
		blogFileUpload.setStore(id);
		List<UploadedFile> uploadedFiles = fileService.upload(blogFileUpload);
		return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
	}

	@PostMapping("folder")
	public ResponseEntity<Void> createFolder(@RequestParam("path") String path,
			@RequestParam(name = "parent", required = false) Integer parent) throws LogicException {
		if (Validators.isEmptyOrNull(path, true)) {
			throw new LogicException("file.create.emptyPath", "文件夹地址不能为空");
		}
		BlogFile blogFile = new BlogFile();
		if (parent != null) {
			BlogFile _parent = new BlogFile();
			_parent.setId(parent);
			blogFile.setParent(_parent);
		}
		blogFile.setPath(path);
		blogFile.setType(BlogFileType.DIRECTORY);
		fileService.createFolder(blogFile);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("file/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		fileService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("file/{id}/properties")
	public BlogFileProperties pro(@PathVariable("id") int id) throws LogicException {
		return fileService.getBlogFileProperties(id);
	}

	@PostMapping(value = "file")
	public ResponseEntity<Void> copy(@RequestParam("id") Integer id, @RequestParam("folderPath") String folderPath)
			throws LogicException {
		if (Validators.isEmptyOrNull(folderPath, true)) {
			throw new LogicException("file.copy.emptyFolderPath", "目标文件夹地址不能为空");
		}
		fileService.copy(id, folderPath);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PatchMapping(value = "file/{id}", params = { "name" })
	public ResponseEntity<Void> rename(@PathVariable("id") Integer id, @RequestParam("name") String name)
			throws LogicException {
		if (Validators.isEmptyOrNull(name, true)) {
			throw new LogicException("file.rename.emptyNewName", "新文件名不能为空");
		}
		fileService.rename(id, name);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "file/{id}", params = { "path" })
	public ResponseEntity<Void> move(@PathVariable("id") Integer id, @RequestParam("path") String path)
			throws LogicException {
		if (Validators.isEmptyOrNull(path, true)) {
			throw new LogicException("file.move.emptyDestPath", "目标地址不能为空");
		}
		fileService.move(id, path);
		return ResponseEntity.noContent().build();
	}

	private String rename(Base64FileUpload base64FileUpload) {
		// 重命名图片
		// chrome中，复制文件名总是为image.png|.jpeg?
		String name = base64FileUpload.getName();
		String ext = FileUtils.getFileExtension(name);
		return "base64_" + StringUtils.uuid() + (ext.isEmpty() ? "" : "." + ext);
	}
}
