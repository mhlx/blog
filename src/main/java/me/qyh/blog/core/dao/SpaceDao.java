package me.qyh.blog.core.dao;

import java.util.List;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.vo.SpaceQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface SpaceDao {

	/**
	 * 根据别名查询空间
	 * 
	 * @param alias
	 *            空间别名
	 * @return 如果不存在，返回null
	 */
	Space selectByAlias(String alias);

	/**
	 * 根据空间名查询空间
	 * 
	 * @param name
	 *            空间名
	 * @return 如果不存在,返回null
	 */
	Space selectByName(String name);

	/**
	 * 更新空间
	 * 
	 * @param space
	 *            待更新的空间
	 */
	void update(Space space);

	/**
	 * 查询空间
	 * 
	 * @param param
	 *            查询参数
	 * @return 空间集合
	 */
	List<Space> selectByParam(SpaceQueryParam param);

	/**
	 * 插入空间
	 * 
	 * @param space
	 *            待插入的空间
	 */
	void insert(Space space);

	/**
	 * 根据id查询空间
	 * 
	 * @param id
	 *            空间id
	 * @return 如果不存在，返回null
	 */
	Space selectById(Integer id);

	/**
	 * 将所有空间全部置为非默认状态
	 */
	void resetDefault();

	/**
	 * 查询默认空间
	 * 
	 * @return 如果没有默认空间返回null
	 */
	Space selectDefault();

	/**
	 * 删除空间
	 * 
	 * @param id
	 */
	void deleteById(Integer id);

	/**
	 * 检查是否存在被某锁保护的空间
	 * 
	 * @param lockId
	 * @return
	 */
	boolean checkExistsByLockId(String lockId);

}
