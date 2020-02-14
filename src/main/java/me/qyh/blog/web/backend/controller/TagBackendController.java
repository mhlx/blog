package me.qyh.blog.web.backend.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.service.TagService;
import me.qyh.blog.utils.WebUtils;

@Controller
@RequestMapping("console")
public class TagBackendController {

	private final TagService tagService;

	public TagBackendController(TagService tagService) {
		super();
		this.tagService = tagService;
	}

	@GetMapping("tags")
	public String index(Model model) {
		model.addAttribute("tags", tagService.getAllTags());
		return "console/tag/index";
	}

	@GetMapping(value = "tags", headers = WebUtils.AJAX_HEADER)
	@ResponseBody
	public List<Tag> tags() {
		return tagService.getAllTags();
	}

	@PostMapping("tag/save")
	@ResponseBody
	public int save(@RequestBody @Valid Tag tag) {
		return tagService.saveTag(tag);
	}

	@PostMapping("tag/{id}/delete")
	@ResponseBody
	public void delete(@PathVariable("id") int id) {
		tagService.deleteTag(id);
	}

	@PostMapping("tag/{id}/update")
	@ResponseBody
	public void update(@PathVariable("id") int id, @RequestBody @Valid Tag tag) {
		tag.setId(id);
		tagService.updateTag(tag);
	}
}
