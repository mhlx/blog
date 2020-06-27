package me.qyh.blog.mapper;

import me.qyh.blog.entity.ArticleTag;
import me.qyh.blog.vo.ArticleTagStatistic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper {

    void deleteByArticle(int articleId);

    void deleteByTag(int tagId);

    void insert(ArticleTag articleTag);

    List<ArticleTagStatistic> selectStatistic(@Param("queryPrivate") boolean queryPrivate,
                                              @Param("categoryId") Integer categoryId);

}