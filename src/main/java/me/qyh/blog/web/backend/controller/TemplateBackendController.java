package me.qyh.blog.web.backend.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.entity.Template;
import me.qyh.blog.entity.TemplateValidator;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.service.TemplateService;
import me.qyh.blog.vo.TemplateQueryParam;

@Controller
@RequestMapping("console")
public class TemplateBackendController {

	@InitBinder("template")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(new TemplateValidator());
	}

	private final TemplateService templateService;

	public TemplateBackendController(TemplateService templateService) {
		super();
		this.templateService = templateService;
	}

	@GetMapping("templates")
	public String index(TemplateQueryParam param, Model model) {
		if (!param.hasPageSize()) {
			param.setPageSize(10);
		}
		model.addAttribute("page", templateService.queryTemplate(param));
		return "console/template/index";
	}

	@GetMapping("previewTemplates")
	public String previewTemplates(Model model) {
		model.addAttribute("templates", templateService.getPreviewTemplates());
		return "console/template/preview";
	}

	@PostMapping("previewTemplates/{id}/delete")
	@ResponseBody
	public void deletePreviewTemplate(@PathVariable("id") int id) {
		templateService.deletePreviewTemplate(id);
	}

	@GetMapping("templates/{id}/edit")
	public String edit(@PathVariable("id") int id, Model model) {
		model.addAttribute("template", templateService.getTemplate(id)
				.orElseThrow(() -> new ResourceNotFoundException("template.notExists", "模板不存在")));
		return "console/template/edit";
	}

	@GetMapping("template/write")
	public String write() {
		return "console/template/write";
	}

	@PostMapping("template/save")
	@ResponseBody
	public int save(@Valid @RequestBody Template template) {
		return templateService.registerTemplate(template);
	}

	@PostMapping("template/preview")
	@ResponseBody
	public Map<String, Object> preview(@Valid @RequestBody Template template) {
		templateService.registerPreviewTemplate(template);
		if (template.getPattern() != null) {
			return Map.of("pattern", template.getPattern(), "definitely", template.isDefinitelyPattern());
		}
		return Map.of();
	}

	@PostMapping("previewTemplates/clear")
	@ResponseBody
	public void clearPreview() {
		templateService.clearPreviewTemplates();
	}

	@PostMapping("previewTemplates/merge")
	@ResponseBody
	public void merge() {
		templateService.mergePreviewTemplates();
	}

	@PostMapping("templates/{id}/delete")
	@ResponseBody
	public void save(@PathVariable("id") int id) {
		templateService.deleteTemplate(id);
	}

	@PostMapping("templates/{id}/update")
	@ResponseBody
	public void update(@PathVariable("id") int id, @Valid @RequestBody Template template) {
		template.setId(id);
		templateService.updateTemplate(template);
	}

	@GetMapping("defaultTemplates")
	@ResponseBody
	public List<Template> defaultTemplates() {
		return templateService.getDefaultTemplates();
	}

}
