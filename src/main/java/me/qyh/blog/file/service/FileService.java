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
package me.qyh.blog.file.service;

import java.util.List;
import java.util.Map;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.file.entity.BlogFile;
import me.qyh.blog.file.store.FileStore;
import me.qyh.blog.file.vo.BlogFilePageResult;
import me.qyh.blog.file.vo.BlogFileQueryParam;
import me.qyh.blog.file.vo.BlogFileUpload;
import me.qyh.blog.file.vo.FileStatistics;
import me.qyh.blog.file.vo.UploadedFile;

/**
 * 
 * @author Administrator
 *
 */
public interface FileService {

	/**
	 * 上传文件
	 * 
	 * @param upload
	 *            文件对象
	 * @return 上传详情
	 * @throws LogicException
	 *             上传过程中发生逻辑异常
	 */
	List<UploadedFile> upload(BlogFileUpload upload) throws LogicException;

	/**
	 * 创建文件夹
	 * 
	 * @param toCreate
	 *            待创建的文件夹
	 * @throws LogicException
	 *             创建逻辑异常
	 */
	BlogFile createFolder(BlogFile toCreate) throws LogicException;

	/**
	 * 分页查询文件
	 * 
	 * @param param
	 *            查询参数
	 * @return 文件分页对象
	 * @throws LogicException
	 *             查询逻辑异常
	 */
	BlogFilePageResult queryBlogFiles(BlogFileQueryParam param) throws LogicException;

	/**
	 * 获取文件属性
	 * 
	 * @param id
	 *            文件id
	 * @return 文件属性map
	 * @throws LogicException
	 *             查询属性异常
	 */
	Map<String, Object> getBlogFileProperty(Integer id) throws LogicException;

	/**
	 * 获取可存储文件的文件储存器
	 * 
	 * @return 文件服务列表
	 */
	List<FileStore> allStorableStores();

	/**
	 * 删除文件树节点(不会删除实际的物理文件)
	 * 
	 * @see FileService#clear()
	 * @param id
	 *            文件id
	 * @throws LogicException
	 *             删除过程中逻辑异常
	 */
	void delete(Integer id) throws LogicException;

	/**
	 * 查询<b>文件</b>
	 * <p>
	 * 用于DataTag
	 * </p>
	 * 
	 * @param path
	 *            路径，指向一个文件夹，如果为null或空或为/，查询根目录
	 * @param param
	 *            查询参数
	 * @return
	 */
	PageResult<BlogFile> queryFiles(String path, BlogFileQueryParam param);

	/**
	 * 拷贝文件
	 * 
	 * @param sourceId
	 *            源文件ID
	 * @param folderPath
	 *            目标文件夹
	 * @throws LogicException
	 */
	void copy(Integer sourceId, String folderPath) throws LogicException;

	/**
	 * 移动文件至新的路径
	 * <p>
	 * <b>只有文件才能够被移动，无法更改文件后缀名</b>
	 * </p>
	 * 
	 * @param sourceId
	 *            原文件id
	 * @param newPath
	 *            新路径，该路径指向一个文件
	 * @throws LogicException
	 */
	void move(Integer sourceId, String newPath) throws LogicException;

	/**
	 * 重命名文件
	 * 
	 * @param id
	 *            文件ID
	 * @param newName
	 *            新文件名，不包括后缀
	 * @throws LogicException
	 */
	void rename(Integer id, String newName) throws LogicException;

	/**
	 * 清理文件
	 */
	void clear();

	/**
	 * 统计文件
	 * 
	 * @return
	 */
	FileStatistics queryFileStatistics();

}
