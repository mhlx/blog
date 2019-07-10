package me.qyh.blog.core.plugin;

/**
 * 模板注册
 * <p>
 * <b>仅用于插件！！！</b>
 * </p>
 * 
 * @author wwwqyhme
 *
 */
public interface TemplateRegistry {

	/**
	 * 注册为系统模板
	 * <p>
	 * 如果路径已经存在，则替换，如果系统模板不存在，则增加新的系统模板
	 * </p>
	 * 
	 * @param path
	 * @param template
	 * @return
	 */
	TemplateRegistry registerSystemTemplate(String path, String template);

	/**
	 * 注册全局默认模板片段
	 * <p>
	 * 如果模板片段已经存在，则替换，否则增加新的模板片段
	 * </p>
	 * 
	 * @param name
	 * @param template
	 * @param callable
	 * @return
	 */
	TemplateRegistry registerGlobalFragment(String name, String template, boolean callable);

	/**
	 * 注册一个简单的模板，这个模板不会匹配任何路径，只是使用模板名和模板内容做映射，仅用于插件的使用
	 * 
	 * @param name
	 * @param template
	 * @return
	 */
	TemplateRegistry registerPluginTemplate(String pluginName, String name, String template);

}
