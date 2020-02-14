package me.qyh.blog.entity;

public class ArticleTag {

	private Integer id;
	private Integer articleId;
	private Integer tagId;

	public ArticleTag() {
		super();
	}

	public ArticleTag(Integer articleId, Integer tagId) {
		super();
		this.articleId = articleId;
		this.tagId = tagId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getArticleId() {
		return articleId;
	}

	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

}
