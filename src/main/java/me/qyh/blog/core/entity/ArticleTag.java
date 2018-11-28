package me.qyh.blog.core.entity;

/**
 * 
 * @author Administrator
 *
 */
public class ArticleTag {

	private Article article;
	private Tag tag;

	/**
	 * default
	 */
	public ArticleTag() {
		super();
	}

	/**
	 * 
	 * @param article
	 *            文章
	 * @param tag
	 *            标签
	 */
	public ArticleTag(Article article, Tag tag) {
		this.article = article;
		this.tag = tag;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

}
