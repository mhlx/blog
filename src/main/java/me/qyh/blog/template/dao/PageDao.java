package me.qyh.blog.template.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.vo.TemplatePageQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface PageDao {

	/**
	 * 根据id查询用户自定义页面
	 * 
	 * @param id
	 *            用户自定义页面id
	 * @return 如果不存在，返回null
	 */
	Page selectById(Integer id);

	/**
	 * 更新用户自定义页面
	 * 
	 * @param page
	 *            待更新的用户自定义页面
	 */
	void update(Page page);

	/**
	 * 插入用户自定义页面
	 * 
	 * @param page
	 *            待插入的用户自定义页面
	 */
	void insert(Page page);

	/**
	 * 查询用户自定义页面数目
	 * 
	 * @param param
	 *            查询参数
	 * @return 数目
	 */
	int selectCount(TemplatePageQueryParam param);

	/**
	 * 查询用户自定义页面集合
	 * 
	 * @param param
	 *            查询参数
	 * @return 结果集
	 */
	List<Page> selectPage(TemplatePageQueryParam param);

	/**
	 * 根据id删除用户自定义页面
	 * 
	 * @param id
	 *            用户自定义页面id
	 */
	void deleteById(Integer id);

	/**
	 * 根据空间和别名查询用户自定义页面
	 * 
	 * @param space
	 *            空间
	 * @param alias
	 *            别名
	 * @param spaceGlobal
	 * @return 如果不存在，返回null
	 */
	Page selectBySpaceAndAlias(@Param("space") Space space, @Param("alias") String alias,
			@Param("spaceGlobal") boolean spaceGlobal);

	/**
	 * 查询某空间下的所有用户自定义页面
	 * 
	 * @param space
	 *            空间
	 * @return 空间下的用户自定义页面集
	 */
	List<Page> selectBySpace(@Param("space") Space space);

	/**
	 * 查询所有自定义页面
	 * 
	 * @return
	 */
	List<Page> selectAll();

	/**
	 * 根据指定id集合查询对应的页面，只会查询一些构建访问链接等必要的信息
	 * 
	 * @param ids
	 *            页面id集合
	 * @return 页面集合
	 */
	List<Page> selectSimpleByIds(Collection<Integer> ids);

}
