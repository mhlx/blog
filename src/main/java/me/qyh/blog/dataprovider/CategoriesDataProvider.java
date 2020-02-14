package me.qyh.blog.dataprovider;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import me.qyh.blog.entity.Category;
import me.qyh.blog.service.CategoryService;
import me.qyh.blog.web.template.tag.DataProvider;

@Component
public class CategoriesDataProvider extends DataProvider<List<Category>> {

	private final CategoryService categoryService;

	public CategoriesDataProvider(CategoryService categoryService) {
		super("categories");
		this.categoryService = categoryService;
	}

	@Override
	public List<Category> provide(Map<String, String> attributesMap) throws Exception {
		return categoryService.getAllCategories();
	}
}
