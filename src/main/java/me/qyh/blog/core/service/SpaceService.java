package me.qyh.blog.core.service;

import java.util.List;
import java.util.Optional;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.SpaceQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface SpaceService {

	/**
	 * 添加空间
	 * 
	 * @param space
	 *            待添加的空间
	 * @return
	 * @throws LogicException
	 *             添加过程中发生逻辑异常
	 */
	Space addSpace(Space space) throws LogicException;

	/**
	 * 更新空间
	 * 
	 * @param space
	 *            待更新的空间
	 * @throws LogicException
	 *             更新过程中发生逻辑异常
	 */
	Space updateSpace(Space space) throws LogicException;

	/**
	 * 查询空间
	 * 
	 * @param param
	 *            查询参数
	 * @return 空间列表
	 */
	List<Space> querySpace(SpaceQueryParam param);

	/**
	 * 根据id查询空间
	 * 
	 * @param id
	 *            空间id
	 * @return
	 * 
	 */
	Optional<Space> getSpace(Integer id);

	/**
	 * 根据别名查询空间
	 * 
	 * @param spaceAlias
	 *            别名
	 * @return
	 */
	Optional<Space> getSpace(String spaceAlias);

	/**
	 * <b>完全的</b>删除一个空间
	 * 
	 * @param id
	 *            空间ID
	 * @throws LogicException
	 *             删除失败
	 */
	void deleteSpace(Integer id) throws LogicException;

}
