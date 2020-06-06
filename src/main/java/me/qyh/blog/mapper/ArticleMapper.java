package me.qyh.blog.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import me.qyh.blog.entity.Article;
import me.qyh.blog.vo.ArticleArchiveStatistic;
import me.qyh.blog.vo.ArticleStatistic;
import me.qyh.blog.vo.ArticleStatusStatistic;
import me.qyh.blog.vo.HandledArticleArchiveQueryParam;
import me.qyh.blog.vo.HandledArticleQueryParam;

@Mapper
public interface ArticleMapper {

	Optional<Article> selectById(int id);

	List<Article> selectByIds(List<Integer> ids);

	int selectCount(HandledArticleQueryParam param);

	List<Article> selectPage(HandledArticleQueryParam param);

	Optional<Article> selectByAlias(String alias);

	void insert(Article article);

	Optional<LocalDateTime> selectMinimumScheduleDate();

	List<Article> selectScheduled(LocalDateTime max);

	void update(Article article);

	List<Article> selectPublished();

	void deleteById(int id);

	void increaseHits(@Param("id") int id, @Param("hits") int i);

	Optional<Article> selectPrev(@Param("article") Article article, @Param("categoryId") Integer categoryId,
			@Param("queryPrivate") boolean queryPrivate);

	Optional<Article> selectNext(@Param("article") Article article, @Param("categoryId") Integer categoryId,
			@Param("queryPrivate") boolean queryPrivate);

	List<ArticleStatusStatistic> selectStatusStatistic(@Param("queryPrivate") boolean queryPrivate,
			@Param("categoryId") Integer categoryId);

	int selectDaysCount(HandledArticleArchiveQueryParam param);

	List<LocalDate> selectDays(HandledArticleArchiveQueryParam param);

	ArticleStatistic selectStatistic(@Param("queryPrivate") boolean queryPrivate,
			@Param("categoryId") Integer categoryId);

	List<ArticleArchiveStatistic> selectArchiveStatistic(@Param("queryPrivate") boolean queryPrivate,
			@Param("categoryId") Integer categoryId);

}