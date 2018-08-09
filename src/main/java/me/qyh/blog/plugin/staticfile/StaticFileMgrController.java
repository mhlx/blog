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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.vo.JsonResult;
import me.qyh.blog.file.vo.UploadedFile;
import me.qyh.blog.plugin.staticfile.validator.StaticFileQueryParamValidator;
import me.qyh.blog.plugin.staticfile.validator.StaticFileUploadValidator;
import me.qyh.blog.plugin.staticfile.vo.FileContent;
import me.qyh.blog.plugin.staticfile.vo.StaticFileQueryParam;
import me.qyh.blog.plugin.staticfile.vo.StaticFileUpload;
import me.qyh.blog.plugin.staticfile.vo.UnzipConfig;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.controller.back.BaseMgrController;

@EnsureLogin
@Controller
@RequestMapping("mgr/static")
public class StaticFileMgrController extends BaseMgrController {

	@Autowired
	private EditablePathResourceHttpRequestHandler handler;
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

	@GetMapping("index")
	public String index(@Validated StaticFileQueryParam staticFileQueryParam, Model model) {
		staticFileQueryParam.setPageSize(configServer.getGlobalConfig().getFilePageSize());
		model.addAttribute("result", handler.query(staticFileQueryParam));
		return "plugin/staticfile/index";
	}

	@GetMapping("query")
	@ResponseBody
	public JsonResult query(@Validated StaticFileQueryParam staticFileQueryParam) throws LogicException {
		staticFileQueryParam.setExtensions(Set.of());
		staticFileQueryParam.setPageSize(configServer.getGlobalConfig().getFilePageSize());
		return new JsonResult(true, handler.query(staticFileQueryParam));
	}

	@GetMapping("edit")
	public String edit(@RequestParam("path") String path, Model model, RedirectAttributes ra) {
		try {
			FileContent fileContent = handler.getEditableFile(path);
			model.addAttribute("file", fileContent);

			String ext = fileContent.getExt().toLowerCase();
			if (ext.equals("htm")) {
				ext = "html";
			}
			return "plugin/staticfile/editor_" + ext;
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/mgr/static/index";
		}
	}

	@PostMapping("edit")
	@ResponseBody
	public JsonResult edit(@RequestParam("path") String path, @RequestParam("content") String content)
			throws LogicException {
		handler.editFile(path, content);
		return new JsonResult(true, new Message("staticFile.edit.success", "文件编辑成功"));
	}

	@PostMapping("upload")
	@ResponseBody
	public JsonResult upload(@Validated StaticFileUpload staticFileUpload, BindingResult result) throws LogicException {
		Optional<JsonResult> validateError = Webs.getFirstError(result);
		if (validateError.isPresent()) {
			return validateError.get();
		}
		List<UploadedFile> uploadedFiles = handler.upload(staticFileUpload);
		return new JsonResult(true, uploadedFiles);
	}

	@PostMapping("copy")
	@ResponseBody
	public JsonResult copy(@RequestParam("path") String path, @RequestParam("destPath") String destPath)
			throws LogicException {
		handler.copy(path, destPath);
		return new JsonResult(true, new Message("staticFile.copy.success", "拷贝成功"));
	}

	@PostMapping("move")
	@ResponseBody
	public JsonResult move(@RequestParam("path") String path, @RequestParam("destPath") String destPath)
			throws LogicException {
		handler.move(path, destPath);
		return new JsonResult(true, new Message("staticFile.move.success", "移动成功"));
	}

	@PostMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam("path") String path) throws LogicException {
		handler.delete(path);
		return new JsonResult(true, new Message("staticFile.delete.success", "删除成功"));
	}

	@PostMapping("createFolder")
	@ResponseBody
	public JsonResult createFolder(@RequestParam("path") String path) throws LogicException {
		handler.createDirectorys(path);
		return new JsonResult(true, new Message("staticFile.create.success", "创建成功"));
	}

	@PostMapping("createFile")
	@ResponseBody
	public JsonResult createFile(@RequestParam("path") String path) throws LogicException {
		handler.createFile(path);
		return new JsonResult(true, new Message("staticFile.create.success", "创建成功"));
	}

	@PostMapping("unzip")
	@ResponseBody
	public JsonResult unzip(@RequestParam("zipPath") String zipPath, UnzipConfig config) throws LogicException {
		if (config.getPath() == null) {
			return new JsonResult(false, new Message("file.unzip.emptyPath", "zip文件路径不能为空"));
		}
		handler.unzip(zipPath, config);
		return new JsonResult(true, new Message("staticFile.unzip.success", "解压缩成功"));
	}

	@PostMapping("zip")
	@ResponseBody
	public JsonResult zip(@RequestParam("path") String path, @RequestParam("zipPath") String zipPath)
			throws LogicException {
		handler.packZip(path, zipPath);
		return new JsonResult(true, new Message("staticFile.zip.success", "压缩成功"));
	}
}
