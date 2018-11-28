package me.qyh.blog.core.vo;

import me.qyh.blog.core.message.Message;

/**
 * 用于Json结果的返回
 * 
 * @author mhlx
 *
 */
public class JsonResult {

	private boolean success;
	private Object data;

	private Message message;

	private String code;

	/**
	 * @param success
	 *            是否成功
	 * @param data
	 *            数据
	 */
	public JsonResult(boolean success, Object data) {
		this.success = success;
		this.data = data;
	}

	/**
	 * 
	 * @param success
	 *            是否成功
	 * @param message
	 *            信息
	 */
	public JsonResult(boolean success, Message message) {
		this.success = success;
		this.message = message;
		this.code = message.getCodes()[0];
	}

	/**
	 * 
	 * @param success
	 *            是否成功
	 */
	public JsonResult(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
		this.code = message.getCodes()[0];
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "JsonResult [success=" + success + ", data=" + data + ", message=" + message + "]";
	}

}
