package me.qyh.blog.template;

import me.qyh.blog.core.util.StringUtils;

public interface PathTemplate extends Template {

	String getRelativePath();

	default boolean hasPathVariable() {
		return StringUtils.substringBetween(getRelativePath(), "{", "}") != null;
	}

}
