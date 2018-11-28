package me.qyh.blog.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.util.WebUtils;

import me.qyh.blog.core.entity.LockKey;

/**
 * 锁辅助类
 * 
 * @author Administrator
 *
 */
public final class LockHelper {

	private static final String LOCKKEY_SESSION_KEY = LockHelper.class.getName() + ".lockKeys";

	private LockHelper() {

	}

	/**
	 * 从请求中获取中获取所有锁钥匙
	 * 
	 * @param request
	 *            当前请求
	 */
	@SuppressWarnings("unchecked")
	public static List<LockKey> getKeys(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		return (List<LockKey>) session.getAttribute(LOCKKEY_SESSION_KEY);
	}

	/**
	 * 在session中为对应锁增加钥匙
	 * 
	 * @param request
	 *            当前请求
	 * @param key
	 *            用户提供的钥匙
	 * @param resourceId
	 *            资源Id
	 */
	public static void addKey(HttpServletRequest request, LockKey key) {
		HttpSession session = request.getSession();
		synchronized (WebUtils.getSessionMutex(session)) {
			List<LockKey> keys = getKeys(request);
			if (keys == null) {
				keys = new ArrayList<>();
			} else {
				keys.removeIf(_key -> _key.lockId().equals(key.lockId()));
			}
			keys.add(key);
			session.setAttribute(LOCKKEY_SESSION_KEY, keys);
		}
	}
}
