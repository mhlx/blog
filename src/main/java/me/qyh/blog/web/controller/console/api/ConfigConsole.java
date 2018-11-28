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
