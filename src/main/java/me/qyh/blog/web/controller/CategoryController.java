package me.qyh.blog.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Category;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.CategoryService;

@Authenticated
@RestController
@RequestMapping("api")
public class CategoryController {

	private final CategoryService categoryService;
	private final BlogProperties blogProperties;

	public CategoryController(CategoryService categoryService, BlogProperties blogProperties) {
		super();
		this.categoryService = categoryService;
		this.blogProperties = blogProperties;
	}

	@GetMapping("categories")
	public List<Category> getCategories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("categories/{name}")
	public Category getCategory(@PathVariable("name") String name) {
		return categoryService.getCategory(name)
				.orElseThrow(() -> new ResourceNotFoundException("category.notExists", "分类不存在"));
	}

	@PostMapping("category")
	public ResponseEntity<Integer> save(@RequestBody @Valid Category category) {
		int id = categoryService.saveCategory(category);
		return ResponseEntity.created(blogProperties.buildUrl("api/categories/" + category.getName())).body(id);
	}

	@DeleteMapping("categories/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") int id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("categories/{id}")
	public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody @Valid Category category) {
		category.setId(id);
		categoryService.updateCategory(category);
		return ResponseEntity.noContent().build();
	}
}
