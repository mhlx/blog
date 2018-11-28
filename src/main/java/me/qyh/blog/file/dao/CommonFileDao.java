package me.qyh.blog.file.dao;

import me.qyh.blog.file.entity.CommonFile;

/**
 * @author Administrator
 *
 */
public interface CommonFileDao {

	/**
	 * 根据id删除普通文件记录
	 * 
	 * @param id
	 */
	void deleteById(Integer id);

	/**
	 * 插入普通文件记录
	 * 
	 * @param commonFile
	 */
	void insert(CommonFile commonFile);
}
