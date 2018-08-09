package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.message.Message;

/**
 * 
 * 解锁结果
 * 
 * @since 6.6
 * @author wwwqyhme
 *
 */
public class UnlockResult {

	private boolean unlocked;// 是否解开锁
	private Message error;
	private Lock lock;

	public Message getError() {
		return error;
	}

	public void setError(Message error) {
		this.error = error;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}
}
