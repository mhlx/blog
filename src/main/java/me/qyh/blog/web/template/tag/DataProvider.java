package me.qyh.blog.web.template.tag;

import java.util.Map;

import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;

/**
 * 用于data标签提供数据
 * 
 * @author wwwqyhme
 *
 */
public abstract class DataProvider<T> {

	private final String name;// 数据名，唯一

	public DataProvider(String name) {
		super();
		this.name = name;
	}

	/**
	 * 
	 * @param attributesMap <b>不包含name属性和alias属性</b>
	 * @return {@link LogicException}
	 * @return {@link ResourceNotFoundException}
	 * @return
	 */
	public abstract T provide(Map<String, String> attributesMap) throws Exception;

	/**
	 * should execute query in a readonly transaction
	 * 
	 * @return
	 */
	protected boolean shouldExecuteInTransaction() {
		return true;
	}

	public String getName() {
		return name;
	}

}
