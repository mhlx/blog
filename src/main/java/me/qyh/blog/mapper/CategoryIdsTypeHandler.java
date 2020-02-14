package me.qyh.blog.mapper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import me.qyh.blog.entity.Category;
import me.qyh.blog.utils.StringUtils;

public class CategoryIdsTypeHandler extends GroupConcatTypeHandler<Set<Category>> {

	@Override
	protected Set<Category> get(String str) {
		if (StringUtils.isNullOrBlank(str)) {
			return Set.of();
		}
		return Arrays.stream(str.split(",")).distinct().map(Integer::parseInt).map(Category::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
