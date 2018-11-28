package me.qyh.blog.file.store;

import java.util.List;
import java.util.Optional;

/**
 * 文件服务管理器
 * 
 * @author Administrator
 *
 */
public interface FileManager {

	/**
	 * 获取所有的文件服务
	 * 
	 * @return
	 */
	List<FileStore> getAllStores();
	
	/**
	 * 根据id查询文件服务
	 * 
	 * @param id
	 *            服务id
	 * @return
	 */
	Optional<FileStore> getFileStore(int id);
	
	
	/**
	 * 新增一个文件存储器
	 * @param store
	 */
	void addFileStore(FileStore store);

}
