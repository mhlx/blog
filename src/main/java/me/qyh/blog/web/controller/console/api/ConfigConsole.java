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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.GlobalConfig;
import me.qyh.blog.core.validator.GlobalConfigValidator;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RequestMapping("api/console")
@RestController
public class ConfigConsole extends BaseMgrController {

	@Autowired
	private ConfigServer configServer;
	@Autowired
	private GlobalConfigValidator globalConfigValidator;

	@InitBinder(value = "globalConfig")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(globalConfigValidator);
	}

	@GetMapping("config")
	public GlobalConfig index() {
		return configServer.getGlobalConfig();
	}

	@PutMapping("config")
	public ResponseEntity<Void> update(@Validated @RequestBody GlobalConfig globalConfig) {
		configServer.updateGlobalConfig(globalConfig);
		return ResponseEntity.noContent().build();
	}

}
