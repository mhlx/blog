package me.qyh.blog.vo;

import me.qyh.blog.entity.Article.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ArticleStatistic {

    private LocalDateTime firstPubDate;
    private LocalDateTime lastPubDate;
    private LocalDateTime lastModifyDate;
    private long hits;
    private long comments;

    private List<ArticleCategoryStatistic> categoryStatistics;
    private List<ArticleStatusStatistic> statusStatistics;
    private List<ArticleTagStatistic> tagStatistics;
    private List<ArticleArchiveStatistic> archiveStatistics;

    public List<ArticleCategoryStatistic> getCategoryStatistics() {
        return categoryStatistics;
    }

    public List<ArticleStatusStatistic> getStatusStatistics() {
        return statusStatistics;
    }

    public LocalDateTime getFirstPubDate() {
        return firstPubDate;
    }

    public void setFirstPubDate(LocalDateTime firstPubDate) {
        this.firstPubDate = firstPubDate;
    }

    public LocalDateTime getLastPubDate() {
        return lastPubDate;
    }

    public void setLastPubDate(LocalDateTime lastPubDate) {
        this.lastPubDate = lastPubDate;
    }

    public LocalDateTime getLastModifyDate() {
        return lastModifyDate;
    }

    public void setLastModifyDate(LocalDateTime lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

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

    public void setCategoryStatistics(List<ArticleCategoryStatistic> categoryStatistics) {
        this.categoryStatistics = categoryStatistics;
    }

    public void setStatusStatistics(List<ArticleStatusStatistic> statusStatistics) {
        this.statusStatistics = statusStatistics;
    }

    public List<ArticleTagStatistic> getTagStatistics() {
        return tagStatistics;
    }

    public void setTagStatistics(List<ArticleTagStatistic> tagStatistics) {
        this.tagStatistics = tagStatistics;
    }

    public List<ArticleArchiveStatistic> getArchiveStatistics() {
        return archiveStatistics;
    }

    public void setArchiveStatistics(List<ArticleArchiveStatistic> archiveStatistics) {
        this.archiveStatistics = archiveStatistics;
    }

    public Integer getCategoryCount() {
        return categoryStatistics == null ? null : categoryStatistics.size();
    }

    public int getTagCount() {
        return tagStatistics == null ? null : tagStatistics.size();
    }

    public int getArticleCount() {
        return this.statusStatistics.stream().filter(s -> s.getStatus() == ArticleStatus.PUBLISHED)
                .mapToInt(ArticleStatusStatistic::getCount).sum();
    }
}
