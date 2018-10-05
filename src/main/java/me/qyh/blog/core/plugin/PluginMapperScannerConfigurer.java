package me.qyh.blog.core.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.spring.mapper.MapperScannerConfigurer;

import me.qyh.blog.core.util.Validators;

public class PluginMapperScannerConfigurer extends MapperScannerConfigurer {

	private final List<String> basePackages = new ArrayList<>();

	public PluginMapperScannerConfigurer(List<String> basePackages) {
		super();
		addBasePackage("me.qyh.blog.core.dao", "me.qyh.blog.file.dao", "me.qyh.blog.template.dao");
		addBasePackage(basePackages.toArray(String[]::new));
	}

	private void addBasePackage(String... basePackages) {
		if (Validators.isEmpty(basePackages)) {
			return;
		}
		this.basePackages.addAll(Arrays.asList(basePackages));
		super.setBasePackage(this.basePackages.stream().collect(Collectors.joining(",")));
	}

}
