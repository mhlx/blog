package me.qyh.blog.plugin.hitstory;

import java.time.LocalDateTime;

import me.qyh.blog.core.entity.Article;

/**
 * @since 5.10
 *
 */
public class HitsHistory extends Article {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String ip;
	private final LocalDateTime time;

	public HitsHistory(Article source, String ip, LocalDateTime time) {
		super(source);
		this.ip = ip;
		this.time = time;

		setContent(null);
		setSummary(null);
	}

	public String getIp() {
		return ip;
	}

	public LocalDateTime getTime() {
		return time;
	}

}
