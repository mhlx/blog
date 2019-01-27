package me.qyh.blog.template.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.vo.FragmentQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface FragmentDao {

	/**
	 * 插入用户模板片段
	 * 
	 * @param fragment
	 *            待插入的用户模板片段
	 */
	void insert(Fragment fragment);

	/**
	 * 根据id删除用户模板片段
	 * 
	 * @param id
	 *            用户模板片段id
	 */
	void deleteById(Integer id);

	/**
	 * 查询用户模板片段集合
	 * 
	 * @param param
	 *            查询参数
	 * @return 用户模板片段集
	 */
	List<Fragment> selectPage(FragmentQueryParam param);

	/**
	 * 查询用户模板片段数目
	 * 
	 * @param param
	 *            查询参数
	 * @return 数目
	 */
	int selectCount(FragmentQueryParam param);

	/**
	 * 更新用户模板片段
	 * 
	 * @param fragment
	 *            待更新的用户模板片段
	 */
	void update(Fragment fragment);

	/**
	 * 根据空间和名称查询用户模板片段
	 * 
	 * @param space
	 *            空间
	 * @param name
	 *            名称
	 * @return 如果不存在，返回null
	 */
	Optional<Fragment> selectBySpaceAndName(@Param("space") Space space, @Param("name") String name);

	/**
	 * 根据id查询用户模板片段
	 * 
	 * @param id
	 *            用户模板片段的id
	 * @return 如果不存在，返回null
	 */
	Optional<Fragment> selectById(Integer id);

	/**
	 * 根据名称查询全局用户模板片段
	 * 
	 * @param name
	 *            名称
	 * @return 如果不存在，返回null
	 */
	Optional<Fragment> selectGlobalByName(String name);

	/**
	 * 查询某个空间下所有的模板片段
	 * 
	 * @param space
	 * @return
	 */
	List<Fragment> selectBySpace(@Param("space") Space space);
}
