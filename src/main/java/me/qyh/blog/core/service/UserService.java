package me.qyh.blog.core.service;

import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.LoginBean;

public interface UserService {

	/**
	 * 登录
	 * 
	 * @param loginBean
	 * @return
	 * @throws LogicException
	 */
	User login(LoginBean loginBean) throws LogicException;

	/**
	 * 更新用户
	 * 
	 * @param user
	 *            当前用户
	 * @param password
	 *            密码
	 * @return
	 * @throws LogicException
	 */
	User update(User user, String password) throws LogicException;

	/**
	 * 获取管理員信息
	 * 
	 * @return
	 */
	User getUser();

}
