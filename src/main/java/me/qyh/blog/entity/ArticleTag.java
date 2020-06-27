package me.qyh.blog.entity;

import java.io.Serializable;

public class ArticleTag implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
