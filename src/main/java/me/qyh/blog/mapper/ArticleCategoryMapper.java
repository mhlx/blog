package me.qyh.blog.mapper;

import me.qyh.blog.entity.ArticleCategory;
import me.qyh.blog.vo.ArticleCategoryStatistic;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArticleCategoryMapper {

    void deleteByArticle(int articleId);

    void insert(ArticleCategory articleCategory);

    void deleteByCategory(int categoryId);

    boolean isArticleExists(int categoryId);

    List<ArticleCategoryStatistic> selectStatistic(boolean queryPrivate);

}
