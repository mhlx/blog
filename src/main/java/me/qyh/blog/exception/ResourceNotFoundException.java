package me.qyh.blog.exception;

/**
 * 用来标记资源不存在
 * 
 * @author wwwqyhme
 *
 */
public class ResourceNotFoundException extends LogicException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String code, String defaultMessage, Object... args) {
		super(code, defaultMessage, args);
	}

}
