package me.qyh.blog.template.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.template.entity.PluginTemplate;

/**
 * 
 * @author Administrator
 *
 */
public interface PluginTemplateDao {

	/**
	 * 查询模板
	 * 
	 * @param pluginName 插件名
	 * @param name       名称
	 * @return
	 */
	Optional<PluginTemplate> selectByPluginNameAndName(@Param("pluginName") String pluginName,
			@Param("name") String name);

	Optional<PluginTemplate> selectById(Integer id);

	/**
	 * 
	 * @param template
	 */
	void update(PluginTemplate template);

	void insert(PluginTemplate template);

	/**
	 * 
	 * @param id
	 */
	void deleteById(Integer id);

	/**
	 * 
	 * @return
	 */
	List<PluginTemplate> selectAll();

}
