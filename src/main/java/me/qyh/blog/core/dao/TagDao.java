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

import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.vo.TagQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface TagDao {

	/**
	 * 插入标签
	 * 
	 * @param tag
	 *            待插入的标签
	 */
	void insert(Tag tag);

	/**
	 * 根据名称查询标签
	 * 
	 * @param name
	 *            标签名
	 * @return 如果不存在，返回null
	 */
	Tag selectByName(String name);

	/**
	 * 查询标签数
	 * 
	 * @param param
	 *            查询参数
	 * @return 数目
	 */
	int selectCount(TagQueryParam param);

	/**
	 * 查询标签集合
	 * 
	 * @param param
	 *            查询参数
	 * @return 标签集
	 */
	List<Tag> selectPage(TagQueryParam param);

	/**
	 * 更新标签
	 * 
	 * @param tag
	 *            待更新的标签
	 */
	void update(Tag tag);

	/**
	 * 查询所有的标签
	 * 
	 * @return
	 */
	List<Tag> selectAll();

	/**
	 * 根据id查询标签
	 * 
	 * @param id
	 *            标签id
	 * @return 如果不存在，返回null
	 */
	Tag selectById(Integer id);

	/**
	 * 删除id对应的标签
	 * 
	 * @param id
	 *            标签id
	 */
	void deleteById(Integer id);
}
