package me.qyh.blog.template.render;

import me.qyh.blog.core.message.Message;

/**
 * 用以重定向页面的异常
 * <p>
 * <b>这个异常不应该被纪录，它仅仅代表着页面需要被跳转，同时也不应该在不需要跳转的时候抛出这个异常</b><br>
 * <b>可以携带一些信息，但这些信息只有当<code>permanently</code>为false的时候才会被传递</b>
 * </p>
 * 
 * @author Administrator
 *
 */
public class RedirectException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String url;
	private final boolean permanently;

	private Message redirectMsg;

	public RedirectException(String url, boolean permanently) {
		super(null, null, false, false);
		this.url = url;
		this.permanently = permanently;
	}

	public String getUrl() {
		return url;
	}

	public boolean isPermanently() {
		return permanently;
	}

	public Message getRedirectMsg() {
		return redirectMsg;
	}

	public void setRedirectMsg(Message redirectMsg) {
		this.redirectMsg = redirectMsg;
	}
}
