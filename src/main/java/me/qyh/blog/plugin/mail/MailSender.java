package me.qyh.blog.plugin.mail;

/**
 * 
 * @author Administrator
 *
 */
public interface MailSender {

	/**
	 * 同步发送邮件
	 * 
	 * @param mb
	 */
	void send(MessageBean mb);

	/**
	 * 异步发送邮件
	 * 
	 * @param mb
	 */
	void sendAsync(MessageBean mb);

}
