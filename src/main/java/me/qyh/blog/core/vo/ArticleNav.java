package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.Article;

/**
 * 上一篇文章，下一篇文章
 * 
 * @author Administrator
 *
 */
public class ArticleNav {

	private Article previous;
	private Article next;

	/**
	 * 构造器
	 * 
	 * @param previous
	 *            上一篇文章
	 * @param next
	 *            下一篇文章
	 */
	public ArticleNav(Article previous, Article next) {
		this.previous = previous;
		this.next = next;
	}

	public Article getPrevious() {
		return previous;
	}

	public void setPrevious(Article previous) {
		this.previous = previous;
	}

	public Article getNext() {
		return next;
	}

	public void setNext(Article next) {
		this.next = next;
	}

}
