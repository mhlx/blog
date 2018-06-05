package me.qyh.blog.core.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.util.Validators;

public class MybatisConfigurer {

	private List<String> basePackages = new ArrayList<>();
	private List<Resource> mapperLocations = new ArrayList<>();
	private List<Resource> typeAliasResources = new ArrayList<>();

	MybatisConfigurer() {
		super();
	}

	List<String> getBasePackages() {
		return basePackages;
	}

	List<Resource> getMapperLocations() {
		return mapperLocations;
	}

	List<Resource> getTypeAliasResources() {
		return typeAliasResources;
	}

	public void addBasePackages(String... basePackages) {
		if (!Validators.isEmpty(basePackages)) {
			this.basePackages.addAll(Arrays.asList(basePackages));
		}
	}

	public void addMapperLocations(Resource... mapperLocations) {
		if (!Validators.isEmpty(mapperLocations)) {
			this.mapperLocations.addAll(Arrays.asList(mapperLocations));
		}
	}

	public void addTypeAliasResources(Resource... typeAliasResources) {
		if (!Validators.isEmpty(typeAliasResources)) {
			this.typeAliasResources.addAll(Arrays.asList(typeAliasResources));
		}
	}

}
