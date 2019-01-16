package me.qyh.blog.file.store;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.file.entity.CommonFile;

/**
 * 文件存储器
 * <p>
 * <b>执行文件操作的时候(写入|删除|拷贝|移动)等，可能需要额外的同步</b>
 * </p>
 * 
 * @author Administrator
 *
 */
public interface FileStore {

	/**
	 * 储存文件
	 * 
	 * @param key
	 *            文件路径
	 * @param multipartFile
	 *            文件
	 * @return 储存成功后的文件信息
	 * @throws LogicException
	 */
	CommonFile store(String key, MultipartFile multipartFile) throws LogicException;

	/**
	 * 存储器ID
	 * 
	 * @return
	 */
	int id();

	/**
	 * 删除物理文件
	 * 
	 * @param key
	 *            文件路径
	 * @return true:删除成功|文件不存在，无需删除 false :删除失败(可能占用中)
	 */
	boolean delete(String key);

	/**
	 * 删除文件夹下物理文件
	 * 
	 * @param key
	 *            文件路径
	 * @return true:如果文件夹不存在或者全部文件删除成功
	 */
	boolean deleteBatch(String key);

	/**
	 * 获取文件的访问路径
	 * 
	 * @param key
	 *            文件路径
	 * @return
	 */
	String getUrl(String key);

	/**
	 * 获取缩略图路径
	 * 
	 * @param key
	 *            文件路径
	 * @return
	 */
	Optional<ThumbnailUrl> getThumbnailUrl(String key);

	/**
	 * 是否能够存储该文件
	 * 
	 * @param multipartFile
	 * @return
	 */
	boolean canStore(MultipartFile multipartFile);

	/**
	 * 存储器名称
	 * 
	 * @return
	 */
	String name();

	/**
	 * 是否只读
	 * 
	 * @return
	 */
	boolean readOnly();

	/**
	 * 拷贝<b>文件</b>
	 * <p>
	 * 如果目标地址存在文件，则覆盖
	 * </p>
	 * 
	 * @param oldPath
	 *            源路径
	 * @param path
	 *            现路径
	 * @return 拷贝成功|失败
	 */
	boolean copy(String oldPath, String path);

	/**
	 * 移动<b>文件</b>
	 * <p>
	 * 如果目标地址存在文件，则覆盖
	 * </p>
	 * 
	 * @param oldPath
	 *            原路径
	 * @param path
	 *            新路径
	 * @return
	 */
	boolean move(String oldPath, String path);

	/**
	 * 
	 * 预处理文件
	 * 
	 * @param file
	 * @return
	 */
	default MultipartFile preHandler(MultipartFile file) throws LogicException {
		return file;
	}

	/**
	 * 返回文件的属性
	 * 
	 * @param key
	 * @return 如果文件不存在或者没有其他属性，返回null或者空
	 */
	default Map<Message, String> getProperties(String key) {
		return Map.of();
	}

	/**
	 * 获取某个文件的存储顺序，数值越大优先级越高
	 * 
	 * @param file
	 * @return
	 */
	default int getOrder(MultipartFile file) {
		return 0;
	}

}
