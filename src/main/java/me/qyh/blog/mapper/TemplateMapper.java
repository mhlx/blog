package me.qyh.blog.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import me.qyh.blog.entity.Template;
import me.qyh.blog.vo.TemplateQueryParam;

@Mapper
public interface TemplateMapper {

	Optional<Template> selectEnabledByName(String name);

	Optional<Template> selectEnabledByPattern(String pattern);
	

	void insert(Template template);

	void deleteById(int id);

	void update(Template template);

	Optional<Template> selectById(int id);

	List<Template> selectPage(TemplateQueryParam param);

	int selectCount(TemplateQueryParam param);

	List<Template> selectEnabled();

}
