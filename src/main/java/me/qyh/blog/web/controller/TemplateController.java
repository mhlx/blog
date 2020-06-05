package me.qyh.blog.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
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

import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Template;
import me.qyh.blog.entity.TemplateValidator;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.TemplateService;
import me.qyh.blog.service.TemplateService.SystemTemplate;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.vo.TemplateQueryParam;
import me.qyh.blog.web.template.TemplateRequestMappingHandlerMapping;
import me.qyh.blog.web.template.TemplateRequestMappingHandlerMapping.TemplateDataPattern;

@Authenticated
@RestController
@RequestMapping("api")
public class TemplateController {

	@InitBinder("template")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(new TemplateValidator());
	}

	private final TemplateService templateService;
	private final BlogProperties blogProperties;

	public TemplateController(TemplateService templateService, BlogProperties blogProperties) {
		super();
		this.templateService = templateService;
		this.blogProperties = blogProperties;
	}

	@GetMapping("templates")
	public PageResult<Template> index(TemplateQueryParam param) {
		return templateService.queryTemplate(param);
	}

	@GetMapping("previewTemplates")
	public List<Template> previewTemplates() {
		return templateService.getPreviewTemplates();
	}

	@DeleteMapping("previewTemplates/{id}")
	public ResponseEntity<?> deletePreviewTemplate(@PathVariable("id") int id) {
		templateService.deletePreviewTemplate(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("templates/{id}")
	public ResponseEntity<Template> getTemplate(@PathVariable("id") int id) {
		return ResponseEntity.of(templateService.getTemplate(id));
	}

	@PostMapping("template")
	public ResponseEntity<Integer> save(@Valid @RequestBody Template template) {
		int id = templateService.registerTemplate(template);
		return ResponseEntity.created(blogProperties.buildUrl("api/templates/" + id)).body(id);
	}

	@PostMapping("previewTemplate")
	public ResponseEntity<Map<String, Object>> preview(@Valid @RequestBody Template template) {
		int id = templateService.registerPreviewTemplate(template);
		Map<String, Object> map;
		if (template.getPattern() != null) {
			map = new HashMap<>();
			map.put("pattern", template.getPattern());
			map.put("definitely", template.isDefinitelyPattern());
			map.put("url", blogProperties.buildUrlString(FileUtils.cleanPath(template.getPattern())));
		} else {
			map = Map.of();
		}
		return ResponseEntity.created(blogProperties.buildUrl("api/previewTemplates/" + id)).body(map);
	}

	@DeleteMapping("previewTemplates")
	public ResponseEntity<?> clearPreview() {
		templateService.clearPreviewTemplates();
		return ResponseEntity.noContent().build();
	}

	@PostMapping("previewTemplates/merge")
	public ResponseEntity<?> merge() {
		templateService.mergePreviewTemplates();
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("templates/{id}")
	public ResponseEntity<?> deleteTemplate(@PathVariable("id") int id) {
		templateService.deleteTemplate(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("templates/{id}")
	public ResponseEntity<?> update(@PathVariable("id") int id, @Valid @RequestBody Template template) {
		template.setId(id);
		templateService.updateTemplate(template);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("defaultTemplates")
	public List<SystemTemplate> defaultTemplates() {
		return templateService.getDefaultTemplates();
	}

	@GetMapping("templateDataPatterns")
	public List<TemplateDataPattern> getTemplateDataPatterns() {
		return TemplateRequestMappingHandlerMapping.getTemplateDataPatterns();
	}

}
