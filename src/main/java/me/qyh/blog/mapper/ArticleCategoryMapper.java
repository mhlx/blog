package me.qyh.blog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import me.qyh.blog.entity.ArticleCategory;
import me.qyh.blog.vo.ArticleCategoryStatistic;

@Mapper
public interface ArticleCategoryMapper {

	void deleteByArticle(int articleId);

	void insert(ArticleCategory articleCategory);

	void deleteByCategory(int categoryId);

	boolean isArticleExists(int categoryId);

	List<ArticleCategoryStatistic> selectCount(boolean queryPrivate);

}
