package me.qyh.blog.plugin.mail;

import java.io.Serializable;

public class MessageBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String subject;
	private final boolean html;
	private final String text;
	private String to;

	/**
	 * 
	 * @param subject
	 *            主题
	 * @param html
	 *            是否是html
	 * @param text
	 *            内容
	 */
	public MessageBean(String subject, boolean html, String text) {
		super();
		this.subject = subject;
		this.html = html;
		this.text = text;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isHtml() {
		return html;
	}

	public String getText() {
		return text;
	}

	public String getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "MessageBean [subject=" + subject + ", html=" + html + ", text=" + text + "]";
	}

}
