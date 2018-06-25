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
package me.qyh.blog.template.render.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.MapBindingResult;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.file.entity.BlogFile;
import me.qyh.blog.file.entity.BlogFile.BlogFileType;
import me.qyh.blog.file.service.FileService;
import me.qyh.blog.file.validator.BlogFileQueryParamValidator;
import me.qyh.blog.file.vo.BlogFileQueryParam;

public class FilesDataTagProcessor extends DataTagProcessor<PageResult<BlogFile>> {

	@Autowired
	private FileService fileService;
	@Autowired
	private BlogFileQueryParamValidator validator;
	@Autowired
	private ConfigServer configServer;

	public FilesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<BlogFile> query(Attributes attributes) throws LogicException {

		BlogFileQueryParam param = new BlogFileQueryParam();

		String extensionStr = attributes.getString("extensions").orElse(null);
		if (!Validators.isEmptyOrNull(extensionStr, true)) {
			param.setExtensions(Arrays.stream(extensionStr.split(",")).collect(Collectors.toSet()));
		}
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(0));
		attributes.getEnum("type", BlogFileType.class).ifPresent(param::setType);
		/**
		 * @since 2017.11.25
		 */
		attributes.getBoolean("ignorePaging").ifPresent(param::setIgnorePaging);

		/**
		 * @since 5.7
		 */
		attributes.getString("fileName").ifPresent(param::setName);

		int pageSize = configServer.getGlobalConfig().getFilePageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		validator.validate(param, new MapBindingResult(new HashMap<>(), "blogFileQueryParam"));

		String path = attributes.getString("path").orElse(null);
		return fileService.queryFiles(path, param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("extensions", "pageSize", "currentPage", "type", "ignorePaging", "fileName", "path");
	}

}
