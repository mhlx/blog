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
