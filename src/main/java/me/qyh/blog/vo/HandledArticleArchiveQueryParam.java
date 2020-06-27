package me.qyh.blog.vo;

public class HandledArticleArchiveQueryParam extends ArticleArchiveQueryParam {

    private final Integer categoryId;

    public HandledArticleArchiveQueryParam(ArticleArchiveQueryParam param, Integer categoryId) {
        super(param);
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

}
