package me.qyh.blog.vo;

import me.qyh.blog.entity.Tag;

public class ArticleTagStatistic {

    private Tag tag;
    private int count;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
