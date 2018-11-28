package me.qyh.blog.core.exception;

/**
 * 空间不存在异常
 * 
 * @author Administrator
 *
 */
public class SpaceNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String alias;

	/**
	 * @param alias
	 *            别名
	 */
	public SpaceNotFoundException(String alias) {
		super(null, null, false, false);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
}
