package me.qyh.blog.plugin.oauth;

import me.qyh.blog.core.exception.LogicException;

public interface OauthProvider {

	/**
	 * 获取oauth授权地址
	 * 
	 * @param state       随机码
	 * @param redirectUrl 重定向地址
	 * @return
	 */
	String getAuthorizeUrl(String redirectUrl, String state);

	/**
	 * 验证是否成功登录已经是否与用户匹配
	 * 
	 * @param code
	 * @param state
	 * @param redirectUrl 重定向地址
	 * @throws LogicException
	 */
	void validate(String code, String state, String redirectUrl) throws LogicException;

	/**
	 * 是否匹配
	 * 
	 * @param providerName
	 * @return
	 */
	String getName();

}
