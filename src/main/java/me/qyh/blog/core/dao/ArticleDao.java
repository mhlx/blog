/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.ArticleArchivePageQueryParam;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.ArticleSpaceStatistics;
import me.qyh.blog.core.vo.ArticleStatistics;

/**
 * 
 * @author Administrator
 *
 */
public interface ArticleDao {

	/**
	 * 根据id查询文章
	 * 
	 * @param id
	 * @return 文章，如果id对应的文章不存在，返回null
	 */
	Article selectById(int id);

	/**
	 * 查询截至日期前的待发布文章
	 * 
	 * @param date
	 *            截止日期
	 * @return
	 */
	List<Article> selectScheduled(Timestamp date);

	/**
	 * 更新文章
	 * 
	 * @param article
	 *            待更新的文章
	 */
	void update(Article article);

	/**
	 * 查询文章数量
	 * 
	 * @param param
	 *            查询参数
	 * @return 文章数量
	 */
	int selectCount(ArticleQueryParam param);

	/**
	 * 查询文章列表
	 * 
	 * @param param
	 *            查询参数
	 * @return 文章列表
	 */
	List<Article> selectPage(ArticleQueryParam param);

	/**
	 * 插入文章
	 * 
	 * @param article
	 *            待插入的文章
	 */
	void insert(Article article);

	/**
	 * 根据指定id集合查询对应的文章
	 * <p>
	 * 文章只保留了一些用于分页展示的信息
	 * </p>
	 * 
	 * @param ids
	 *            文章id集合
	 * @return id集合对应的文章集合
	 */
	List<Article> selectPageByIds(Collection<Integer> ids);

	/**
	 * 根据指定id集合查询对应的文章，只会查询一些构建访问链接等必要的信息
	 * 
	 * @param ids
	 *            文章id集合
	 * @return id集合对应的文章集合
	 */
	List<Article> selectSimpleByIds(Collection<Integer> ids);

	/**
	 * 根据文章id删除文章
	 * 
	 * @param id
	 *            文章id
	 */
	void deleteById(Integer id);

	/**
	 * 增加文章的点击量
	 * 
	 * @param id
	 *            文章的id
	 * @param increase
	 *            <strong>增加的</strong>点击量
	 */
	void addHits(@Param("id") Integer id, @Param("hits") int increase);

	/**
	 * 更新文章的点击量
	 * 
	 * @param id
	 *            文章的id
	 * @param currentHits
	 *            <strong>当前的</strong>点击量
	 */
	void updateHits(@Param("id") Integer id, @Param("hits") int currentHits);

	/**
	 * 查询文章点击数
	 * 
	 * @param id
	 * @return
	 */
	int selectHits(Integer id);

	/**
	 * 上一篇文章
	 * 
	 * @param article
	 *            当前文章
	 * @param queryPrivate
	 *            是否查询私有文章
	 * @return 上一篇，如果不存在，返回null
	 */
	Article getPreviousArticle(@Param("article") Article article, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryLock") boolean queryLock);

	/**
	 * 下一篇文章
	 * 
	 * @param article
	 *            当前文章
	 * @param queryPrivate
	 *            是否查询私有文章
	 * @return 下一篇文章，如果不存在，返回null
	 */
	Article getNextArticle(@Param("article") Article article, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryLock") boolean queryLock);

	/**
	 * 查询文章统计
	 * <p>
	 * <b>只会统计已经发布的博客</b>
	 * </p>
	 * 
	 * @param space
	 *            空间，如果为空，则查询全部
	 * @param queryPrivate
	 *            是否查询私人文章
	 * @return 文章统计
	 */
	ArticleStatistics selectStatistics(@Param("space") Space space, @Param("queryPrivate") boolean queryPrivate);

	/**
	 * 查询文章统计(用于后台统计)
	 * <p>
	 * <b>会统计全部文章</b>
	 * </p>
	 * 
	 * @param space
	 *            空间，如果为空，则查询全部
	 * @return 文章统计
	 */
	ArticleStatistics selectAllStatistics(@Param("space") Space space);

	/**
	 * 根据文章的别名查询文章
	 * 
	 * @param alias
	 *            文章别名
	 * @return 文章，如果不存在，返回null
	 */
	Article selectByAlias(String alias);

	/**
	 * 删除锁
	 * 
	 * @param lockId
	 *            锁id
	 */
	void deleteLock(String lockId);

	/**
	 * 查询最小的待发表文章的发布日期
	 * 
	 * @return 如果当前没有任何带发表文章，那么返回null
	 */
	Timestamp selectMinimumScheduleDate();

	/**
	 * 根据alias查询文章的id
	 * 
	 * @param alias
	 * @return
	 */
	Integer selectIdByAlias(String alias);

	/**
	 * 查询某个空间下<b>所有文章</b>的数量
	 * 
	 * @param space
	 * @return
	 */
	int selectCountBySpace(Space space);

	/**
	 * 将某个空间下的文章移到另一个空间下
	 * 
	 * @param oldSpace
	 * @param newSpace
	 */
	void moveSpace(@Param("oldSpace") Space oldSpace, @Param("newSpace") Space newSpace);

	/**
	 * 查询每个空间下文章数目
	 * 
	 * @param queryPrivate
	 * @return
	 */
	List<ArticleSpaceStatistics> selectArticleSpaceStatistics(@Param("queryPrivate") boolean queryPrivate);

	/**
	 * 分页查询已经发布的文章
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<Article> selectPublishedByPage(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 分页查询文章归档日期
	 * 
	 * @param param
	 * @return
	 * @since 7.0
	 */
	List<String> selectArchiveDays(ArticleArchivePageQueryParam param);

	/**
	 * 查询文章归档日期数
	 * 
	 * @param param
	 * @return
	 * @since 7.0
	 */
	int selectArchiveDaysCount(ArticleArchivePageQueryParam param);
}
