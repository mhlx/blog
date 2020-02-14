package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import me.qyh.blog.entity.Category;
import me.qyh.blog.service.CategoryService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class CategoryDataProvider extends DataProviderSupport<Category> {

	private final CategoryService categoryService;

	public CategoryDataProvider(CategoryService categoryService) {
		super("category");
		this.categoryService = categoryService;
	}

	@Override
	public Category provide(Map<String, String> attributesMap) throws Exception {
		String name = attributesMap.get("name");
		if (StringUtils.isEmptyOrWhitespace(name)) {
			BindingResult br = createBindingResult(attributesMap);
			br.rejectValue("name", "NotBlank", "分类名称不能为空");
			throw new BindException(br);
		}
		return categoryService.getCategory(name).orElse(null);
	}
}
