package me.qyh.blog.mapper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.utils.StringUtils;

public class TagIdsTypeHandler extends GroupConcatTypeHandler<Set<Tag>> {

	@Override
	protected Set<Tag> get(String str) {
		if (StringUtils.isNullOrBlank(str)) {
			return Set.of();
		}
		return Arrays.stream(str.split(",")).distinct().map(Integer::parseInt).map(Tag::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

}
