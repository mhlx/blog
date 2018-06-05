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

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.ArticleTag;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.vo.TagCount;

/**
 * 
 * @author Administrator
 *
 */
public interface ArticleTagDao {

	/**
	 * 插入文章标签
	 * 
	 * @param articleTag
	 *            待插入的文章标签
	 */
	void insert(ArticleTag articleTag);

	/**
	 * 根据文章删除该文章所有的标签
	 * 
	 * @param article
	 *            文章
	 */
	void deleteByArticle(Article article);

	/**
	 * 根据标签删除所有涉及该标签的文章标签
	 * 
	 * @param tag
	 *            标签
	 */
	void deleteByTag(Tag tag);

	/**
	 * 合并标签
	 * 
	 * @param src
	 *            原标签
	 * @param dest
	 *            目标标签
	 */
	void merge(@Param("src") Tag src, @Param("dest") Tag dest);

	/**
	 * 查询标签和对应的引用数目<strong>未被使用的标签不会被查询出来</strong>
	 * 
	 * @param space
	 *            空间
	 * @param queryPrivate
	 *            是否查询私有
	 * @return 标签集合
	 */
	List<TagCount> selectTags(@Param("space") Space space, @Param("queryPrivate") boolean queryPrivate);

	/**
	 * 查询标签总数
	 * <p>
	 * 只会查询<b>状态为发布的</b>文章
	 * </p>
	 * 
	 * @param space
	 *            空间
	 * @param queryPrivate
	 *            是否查询私有
	 * @return
	 */
	int selectTagsCount(@Param("space") Space space, @Param("queryPrivate") boolean queryPrivate);

	/**
	 * 查询<b>所有的</b>文章的引用标签的数量
	 * <p>
	 * 用于后台统计
	 * </p>
	 * 
	 * @return
	 */
	int selectAllTagsCount(@Param("space") Space space);

}
