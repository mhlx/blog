package me.qyh.blog.vo;

public class HandledArticleQueryParam extends ArticleQueryParam {

    private final Integer categoryId;
    private final Integer tagId;

    public HandledArticleQueryParam(ArticleQueryParam param, Integer categoryId, Integer tagId) {
        super(param);
        this.categoryId = categoryId;
        this.tagId = tagId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Integer getTagId() {
        return tagId;
    }

}
