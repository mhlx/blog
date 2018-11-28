package me.qyh.blog.template;

/**
 * 模板
 * 
 * @author Administrator
 *
 */
public interface Template {

	/**
	 * 模板分割符
	 */
	String SPLITER = "%";

	/**
	 * 模板前缀，所有的模板名必须以这个开头
	 */
	String TEMPLATE_PREFIX = "Template" + SPLITER;

	/**
	 * 判断是否是模板文件名
	 * 
	 * @param templateName
	 * @return
	 */
	static boolean isTemplate(String templateName) {
		return templateName != null && templateName.startsWith(TEMPLATE_PREFIX);
	}

	/**
	 * 获取模板内容
	 * 
	 * @return
	 */
	String getTemplate();

	/**
	 * 获取模板名称
	 * <p>
	 * 模板名称应该是全局唯一的
	 * </p>
	 * 
	 * @return
	 */
	String getTemplateName();

	/**
	 * 克隆 template
	 * 
	 * @return
	 */
	Template cloneTemplate();

	/**
	 * 是否可被外部调用
	 * 
	 * @return
	 */
	boolean isCallable();

	/**
	 * 判断是否和另一个Template等价
	 * 
	 * @return
	 */
	boolean equalsTo(Template other);

	/**
	 * 是否可以被缓存
	 * 
	 * @return
	 */
	default boolean cacheable() {
		return true;
	}
}
