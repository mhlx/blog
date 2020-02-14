package me.qyh.blog.web.template;

import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * blog custom tag handler base exception
 * 
 * <p>
 * thymeleaf will wrap custom exception as TemplateInputException<br>
 * so it's hard to determine which exception need to handle<br>
 * but if we wrap exception as BlogTemplateProcessingException<br>
 * we could only find BlogTemplateProcessingException in exception chain<br>
 * and use {@link TemplateProcessingWrapException#getCause()} to get our custom
 * exception
 * </p>
 * 
 * @author wwwqyhme
 *
 */
public class TemplateProcessingWrapException extends TemplateProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TemplateProcessingWrapException(Throwable cause) {
		super(null, cause);
	}

	public static TemplateProcessingWrapException wrap(Exception th) {
		return new TemplateProcessingWrapException(th);
	}
}
