package me.qyh.blog.web.controller.console.api;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.template.SystemTemplate;
import me.qyh.blog.template.entity.HistoryTemplate;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.PageValidator;
import me.qyh.blog.template.validator.TemplatePageQueryParamValidator;
import me.qyh.blog.template.vo.TemplatePageQueryParam;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console/template")
public class PageConsole extends BaseMgrController {

	@Autowired
	private TemplatePageQueryParamValidator pageParamValidator;
	@Autowired
	private TemplateService templateService;
	@Autowired
	private PageValidator pageValidator;
	@Autowired
	private ConfigServer configServer;
	@Autowired
	private UrlHelper urlHelper;

	@InitBinder(value = "page")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(pageValidator);
	}

	@InitBinder(value = "templatePageQueryParam")
	protected void initTemplatePageQueryParamBinder(WebDataBinder binder) {
		binder.setValidator(pageParamValidator);
	}

	@GetMapping("pages")
	public PageResult<Page> findPages(@Validated TemplatePageQueryParam templatePageQueryParam) {
		templatePageQueryParam.setPageSize(configServer.getGlobalConfig().getPagePageSize());
		return templateService.queryPage(templatePageQueryParam);
	}

	@PostMapping("page")
	public ResponseEntity<Page> creatPage(@RequestBody @Validated Page page) throws LogicException {
		Page saved = templateService.createPage(page);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping("page/{id}")
	public ResponseEntity<Void> updatePage(@RequestBody @Validated Page page, @PathVariable("id") Integer id)
			throws LogicException {
		page.setId(id);
		templateService.updatePage(page);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("page/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		templateService.deletePage(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("page/{id}/histories")
	public List<HistoryTemplate> getHistory(@PathVariable("id") Integer id) throws LogicException {
		return templateService.queryPageHistory(id);
	}

	@PostMapping("page/{id}/history")
	public ResponseEntity<Void> saveHistory(@PathVariable("id") Integer id, @RequestParam("remark") String remark)
			throws LogicException {
		Optional<Message> optionalError = HistoryTemplateConsole.validRemark(remark);
		if (optionalError.isPresent()) {
			throw new LogicException(optionalError.get());
		}
		templateService.savePageHistory(id, remark);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("defaultPages")
	public List<SystemTemplate> getDefaul() {
		return templateService.getSystemTemplates();
	}

	@GetMapping("page/{id}")
	public ResponseEntity<Page> get(@PathVariable("id") Integer id) {
		Optional<Page> op = templateService.queryPage(id);
		return ResponseEntity.of(op);
	}

	@PostMapping("page/preview")
	public ResponseEntity<PreviewUrl> preview(@RequestBody @Validated Page page) throws LogicException {
		templateService.registerPreview(page);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new PreviewUrl(urlHelper.getUrls().getUrl(page), page.hasPathVariable()));
	}
}
