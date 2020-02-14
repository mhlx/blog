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

import me.qyh.blog.entity.Category;
import me.qyh.blog.service.CategoryService;
import me.qyh.blog.utils.WebUtils;

@Controller
@RequestMapping("console")
public class CategoryBackendController {

	private final CategoryService categoryService;

	public CategoryBackendController(CategoryService categoryService) {
		super();
		this.categoryService = categoryService;
	}

	@GetMapping("categories")
	public String index(Model model) {
		model.addAttribute("categories", categoryService.getAllCategories());
		return "console/category/index";
	}

	@GetMapping(value = "categories", headers = WebUtils.AJAX_HEADER)
	@ResponseBody
	public List<Category> tags() {
		return categoryService.getAllCategories();
	}

	@PostMapping("category/save")
	@ResponseBody
	public int save(@RequestBody @Valid Category category) {
		return categoryService.saveCategory(category);
	}

	@PostMapping("category/{id}/delete")
	@ResponseBody
	public void delete(@PathVariable("id") int id) {
		categoryService.deleteCategory(id);
	}

	@PostMapping("category/{id}/update")
	@ResponseBody
	public void update(@PathVariable("id") int id, @RequestBody @Valid Category category) {
		category.setId(id);
		categoryService.updateCategory(category);
	}
}
