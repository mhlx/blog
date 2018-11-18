/*
 * Copyright 2017 qyh.me
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
package me.qyh.blog.plugin.staticfile;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.file.vo.UploadedFile;
import me.qyh.blog.plugin.staticfile.validator.StaticFileQueryParamValidator;
import me.qyh.blog.plugin.staticfile.validator.StaticFileUploadValidator;
import me.qyh.blog.plugin.staticfile.vo.StaticFilePageResult;
import me.qyh.blog.plugin.staticfile.vo.StaticFileQueryParam;
import me.qyh.blog.plugin.staticfile.vo.StaticFileUpload;
import me.qyh.blog.plugin.staticfile.vo.UnzipConfig;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.controller.console.BaseMgrController;

@EnsureLogin
@RestController
@RequestMapping("api/console")
public class StaticFileConsole extends BaseMgrController {

	@Autowired
	private StaticFileManager handler;
	@Autowired
	private StaticFileQueryParamValidator staticFileParamValidator;
	@Autowired
	private StaticFileUploadValidator staticFileUploadValidator;
	@Autowired
	private ConfigServer configServer;

	@InitBinder(value = "staticFileQueryParam")
	protected void initStaticFileQueryParamBinder(WebDataBinder binder) {
		binder.setValidator(staticFileParamValidator);
	}

	@InitBinder(value = "staticFileUpload")
	protected void initLocalUploadBinder(WebDataBinder binder) {
		binder.setValidator(staticFileUploadValidator);
	}

	@GetMapping("staticFiles")
	public StaticFilePageResult query(@Validated StaticFileQueryParam staticFileQueryParam, Model model) {
		staticFileQueryParam.setPageSize(configServer.getGlobalConfig().getFilePageSize());
		return handler.query(staticFileQueryParam);
	}

	@PutMapping("staticFile")
	public ResponseEntity<Void> edit(@RequestParam("path") String path, @RequestParam("content") String content)
			throws LogicException {
		handler.editFile(path, content);
		return ResponseEntity.ok().build();
	}

	@PostMapping("staticFiles")
	public ResponseEntity<List<UploadedFile>> upload(@Validated StaticFileUpload staticFileUpload, BindingResult result)
			throws LogicException {
		Optional<Message> validateError = Webs.getFirstError(result);
		if (validateError.isPresent()) {
			throw new LogicException(validateError.get());
		}
		List<UploadedFile> uploadedFiles = handler.upload(staticFileUpload);
		return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
	}

	@PostMapping(value = "staticFile", params = { "path", "destPath" })
	public ResponseEntity<Void> copy(@RequestParam("path") String path, @RequestParam("destPath") String destPath)
			throws LogicException {
		handler.copy(path, destPath);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PatchMapping(value = "staticFile", params = { "path", "destPath" })
	public ResponseEntity<Void> move(@RequestParam("path") String path, @RequestParam("destPath") String destPath)
			throws LogicException {
		handler.move(path, destPath);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "staticFile", params = { "path", "name" })
	public ResponseEntity<Void> rename(@RequestParam("path") String path, @RequestParam("name") String name)
			throws LogicException {
		handler.rename(path, name);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("staticFile")
	public ResponseEntity<Void> delete(@RequestParam("path") String path) throws LogicException {
		handler.delete(path);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("staticFolder")
	public ResponseEntity<Void> createFolder(@RequestParam("path") String path) throws LogicException {
		handler.createDirectorys(path);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping(value = "staticFile", params = { "path" })
	public ResponseEntity<Void> createFile(@RequestParam("path") String path) throws LogicException {
		handler.createFile(path);
		return ResponseEntity.status(HttpStatus.CREATED).build();

	}

	@PostMapping("staticFiles")
	public ResponseEntity<Void> unzip(@RequestParam("zipPath") String zipPath, UnzipConfig config)
			throws LogicException {
		if (config.getPath() == null) {
			throw new LogicException("file.unzip.emptyPath", "zip文件路径不能为空");
		}
		handler.unzip(zipPath, config);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("staticZipFile")
	public ResponseEntity<Void> zip(@RequestParam("path") String path, @RequestParam("zipPath") String zipPath)
			throws LogicException {
		handler.packZip(path, zipPath);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
