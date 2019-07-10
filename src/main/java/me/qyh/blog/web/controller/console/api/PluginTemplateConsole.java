package me.qyh.blog.web.controller.console.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.template.entity.PluginTemplate;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.PluginTemplateValidator;

@RestController
@RequestMapping("api/console/template")
public class PluginTemplateConsole {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private PluginTemplateValidator pluginTemplateValidator;

	@InitBinder(value = "pluginTemplate")
	protected void init(WebDataBinder binder) {
		binder.setValidator(pluginTemplateValidator);
	}

	@GetMapping("pluginTemplates")
	public List<PluginTemplate> allPluginTemplates() {
		return templateService.allPluginTemplate();
	}

	@PostMapping("pluginTemplate")
	public ResponseEntity<PluginTemplate> create(@RequestBody @Validated final PluginTemplate pluginTemplate)
			throws LogicException {
		return ResponseEntity.status(HttpStatus.CREATED).body(templateService.savePluginTemplate(pluginTemplate));
	}

	@DeleteMapping("pluginTemplate/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		templateService.deletePluginTemplate(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("pluginTemplate/{id}")
	public ResponseEntity<Void> update(@RequestBody @Validated PluginTemplate pluginTemplate,
			@PathVariable("id") Integer id) throws LogicException {
		pluginTemplate.setId(id);
		templateService.updatePluginTemplate(pluginTemplate);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("defaultPluginTemplates")
	public List<PluginTemplate> getDefaul() {
		return templateService.getDefaultPluginTemplates();
	}

}
