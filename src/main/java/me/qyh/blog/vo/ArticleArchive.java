package me.qyh.blog.vo;

import me.qyh.blog.entity.Article;

import java.time.LocalDate;
import java.util.List;

public class ArticleArchive {

    private final LocalDate date;
    private final List<Article> articles;

    public ArticleArchive(LocalDate date, List<Article> articles) {
        super();
        this.date = date;
        this.articles = articles;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Article> getArticles() {
        return articles;
    }

}
