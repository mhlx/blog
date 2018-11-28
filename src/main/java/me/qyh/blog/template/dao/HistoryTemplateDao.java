package me.qyh.blog.template.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.template.entity.HistoryTemplate;
import me.qyh.blog.template.entity.HistoryTemplate.HistoryTemplateType;

public interface HistoryTemplateDao {

	List<HistoryTemplate> selectByTemplate(@Param("id") Integer id, @Param("type") HistoryTemplateType type);

	void deleteByTemplate(@Param("id") Integer id, @Param("type") HistoryTemplateType type);

	void insert(HistoryTemplate template);

	void deleteById(Integer id);

	HistoryTemplate selectById(Integer id);

	void update(HistoryTemplate historyTemplate);

}
