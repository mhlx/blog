package me.qyh.blog.core.context;

import java.util.List;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.entity.LockKey;

/**
 * 钥匙上下文
 * 
 * @author Administrator
 *
 */
public class LockKeyContext {

	private static final ThreadLocal<List<LockKey>> KEYS_LOCAL = new ThreadLocal<>();

	private LockKeyContext() {
		super();
	}

	/**
	 * 从上下文中获取钥匙
	 * 
	 * @return
	 */
	public static Optional<LockKey> getKey(String lockId) {
		List<LockKey> keys = KEYS_LOCAL.get();
		if (CollectionUtils.isEmpty(keys)) {
			return Optional.empty();
		}
		return keys.stream().filter(key -> key.lockId().equals(lockId)).findAny();
	}

	/**
	 * 清理上下文
	 */
	public static void remove() {
		KEYS_LOCAL.remove();
	}

	/**
	 * 设置上下文
	 * 
	 * @param keysMap
	 */
	public static void set(List<LockKey> keys) {
		KEYS_LOCAL.set(keys);
	}

}
