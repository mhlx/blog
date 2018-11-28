package me.qyh.blog.core.service;

import java.util.Optional;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.TagDetailStatistics;
import me.qyh.blog.core.vo.TagQueryParam;
import me.qyh.blog.core.vo.TagStatistics;

/**
 * 
 * @author Administrator
 *
 */
public interface TagService {

	/**
	 * 分页查询标签
	 * 
	 * 
	 * @param param
	 *            查询参数
	 * @return 标签该分页对象
	 */
	PageResult<Tag> queryTag(TagQueryParam param);

	/**
	 * 更新标签
	 * 
	 * @param tag
	 *            待更新的标签
	 * @param merge
	 *            是否合并已经存在的标签
	 * @return
	 * @throws LogicException
	 *             更新过程中发生逻辑异常
	 */
	Tag updateTag(Tag tag, boolean merge) throws LogicException;

	/**
	 * 删除标签
	 * 
	 * @param id
	 *            标签id
	 * @throws LogicException
	 *             删除过程中发生逻辑异常
	 */
	void deleteTag(Integer id) throws LogicException;

	/**
	 * 統計某個空间下的标签
	 * 
	 * @param space
	 * @return
	 */
	TagDetailStatistics queryTagDetailStatistics(Space space);

	/**
	 * 統計所有的標簽
	 * 
	 * @return
	 */
	TagStatistics queryTagStatistics();

	/**
	 * 根据ID查询指定标签
	 * 
	 * @param id
	 * @return
	 */
	Optional<Tag> getTag(Integer id);

}
