package me.qyh.blog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import me.qyh.blog.entity.ArticleTag;
import me.qyh.blog.vo.ArticleTagStatistic;

@Mapper
public interface ArticleTagMapper {

	void deleteByArticle(int articleId);

	void deleteByTag(int tagId);

	void insert(ArticleTag articleTag);

	List<ArticleTagStatistic> selectCount(@Param("queryPrivate") boolean queryPrivate,
			@Param("categoryId") Integer categoryId);

}