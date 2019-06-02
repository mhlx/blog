package me.qyh.blog.template.vo;

import java.io.Serializable;

import me.qyh.blog.core.message.Message;

/**
 * 导入纪录
 * 
 * @author Administrator
 *
 */
public class ImportRecord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final boolean success;
	private final Message message;

	private ImportType type;

	public enum ImportType {
		NEW, EDIT, NOCHANGE
	}

	public ImportRecord(boolean success, Message message) {
		super();
		this.success = success;
		this.message = message;
	}

	public ImportRecord(ImportType type, Message message) {
		super();
		this.success = true;
		this.message = message;
		this.type = type;
	}

	public boolean isSuccess() {
		return success;
	}

	public Message getMessage() {
		return message;
	}

	public ImportType getType() {
		return type;
	}

	public void setType(ImportType type) {
		this.type = type;
	}

}
