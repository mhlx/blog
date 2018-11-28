package me.qyh.blog.template.render;

/**
 * 
 * @author wwwqyhme
 *
 */
public class MissLockException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String unlockId;

	public MissLockException(String unlockId) {
		super(null, null, false, false);
		this.unlockId = unlockId;
	}

	public String getUnlockId() {
		return unlockId;
	}

}
