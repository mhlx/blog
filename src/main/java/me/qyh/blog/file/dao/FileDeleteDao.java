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
package me.qyh.blog.file.dao;

import java.util.List;

import me.qyh.blog.file.entity.FileDelete;

/**
 * 
 * @author Administrator
 *
 */
public interface FileDeleteDao {

	/**
	 * 删除待删除文件记录
	 * 
	 * @param fileDelete
	 *            待删除文件记录
	 */
	void insert(FileDelete fileDelete);

	/**
	 * 查询所有待删除文件记录
	 * 
	 * @return 纪录集
	 */
	List<FileDelete> selectAll();

	/**
	 * 根据id删除对应的待删除文件记录
	 * 
	 * @param id
	 *            纪录id
	 */
	void deleteById(Integer id);

	/**
	 * 查询路径下所有的待删除文件记录
	 * 
	 * @param key
	 *            路径
	 * @return 结果集
	 */
	List<FileDelete> selectChildren(String key);

	/**
	 * 删除路径下所有的待删除文件记录
	 * 
	 * @param key
	 *            路径
	 */
	void deleteChildren(String key);

}
