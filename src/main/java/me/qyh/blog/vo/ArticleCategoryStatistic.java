package me.qyh.blog.vo;

import me.qyh.blog.entity.Category;

public class ArticleCategoryStatistic {

    private Category category;
    private int count;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
