package me.qyh.blog.web.controller;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Category;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.CategoryService;
import me.qyh.blog.web.template.TemplateDataMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @TemplateDataMapping("categories")
    public List<Category> getCategories() {
        return categoryService.getAllCategories();
    }

    @TemplateDataMapping("categories/{name}")
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
