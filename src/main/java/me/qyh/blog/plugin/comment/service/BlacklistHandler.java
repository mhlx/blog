package me.qyh.blog.plugin.comment.service;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.plugin.comment.vo.IPQueryParam;

/**
 * 用于禁止某些IP评论
 * 
 * @since 6.0
 * @author wwwqyhme
 *
 */
public interface BlacklistHandler {

	/**
	 * 分页查询黑名单中的IP
	 * 
	 * @param param
	 * @return
	 */
	PageResult<String> query(IPQueryParam param);

	/**
	 * 将IP从黑名单中移除
	 * 
	 * @param ip
	 */
	void remove(String ip);

	/**
	 * 将IP加入黑名单
	 * 
	 * @param ip
	 * @throws LogicException
	 */
	void add(String ip) throws LogicException;

	/**
	 * 判断IP是否在黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	boolean match(String ip);
}
