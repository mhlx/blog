package me.qyh.blog.vo;

import java.time.LocalDateTime;

public class MomentStatistic {

    private long hits;
    private long comments;
    private int count;
    private LocalDateTime first;
    private LocalDateTime last;
    private LocalDateTime lastModify;

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public LocalDateTime getFirst() {
        return first;
    }

    public void setFirst(LocalDateTime first) {
        this.first = first;
    }

    public LocalDateTime getLast() {
        return last;
    }

    public void setLast(LocalDateTime last) {
        this.last = last;
    }

    public LocalDateTime getLastModify() {
        return lastModify;
    }

    public void setLastModify(LocalDateTime lastModify) {
        this.lastModify = lastModify;
    }

}
