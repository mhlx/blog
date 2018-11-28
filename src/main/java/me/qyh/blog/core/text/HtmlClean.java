package me.qyh.blog.core.text;

/**
 * html文本清理
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface HtmlClean {

	/**
	 * 清理文本
	 * 
	 * @param html
	 *            待清理的html文本
	 * @return 清理后的html文本
	 */
	String clean(String html);

}
