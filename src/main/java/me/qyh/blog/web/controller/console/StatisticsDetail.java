package me.qyh.blog.web.controller.console;

import me.qyh.blog.core.vo.ArticleDetailStatistics;
import me.qyh.blog.core.vo.CommentStatistics;
import me.qyh.blog.core.vo.NewsStatistics;
import me.qyh.blog.core.vo.TagDetailStatistics;
import me.qyh.blog.file.vo.FileStatistics;
import me.qyh.blog.template.vo.PageStatistics;

class StatisticsDetail {
	private ArticleDetailStatistics articleStatistics;
	private TagDetailStatistics tagStatistics;
	private CommentStatistics commentStatistics;
	private PageStatistics pageStatistics;
	private FileStatistics fileStatistics;
	private NewsStatistics newsStatistics;

	public ArticleDetailStatistics getArticleStatistics() {
		return articleStatistics;
	}

	public void setArticleStatistics(ArticleDetailStatistics articleStatistics) {
		this.articleStatistics = articleStatistics;
	}

	public TagDetailStatistics getTagStatistics() {
		return tagStatistics;
	}

	public void setTagStatistics(TagDetailStatistics tagStatistics) {
		this.tagStatistics = tagStatistics;
	}

	public CommentStatistics getCommentStatistics() {
		return commentStatistics;
	}

	public void setCommentStatistics(CommentStatistics commentStatistics) {
		this.commentStatistics = commentStatistics;
	}

	public PageStatistics getPageStatistics() {
		return pageStatistics;
	}

	public void setPageStatistics(PageStatistics pageStatistics) {
		this.pageStatistics = pageStatistics;
	}

	public FileStatistics getFileStatistics() {
		return fileStatistics;
	}

	public void setFileStatistics(FileStatistics fileStatistics) {
		this.fileStatistics = fileStatistics;
	}

	public NewsStatistics getNewsStatistics() {
		return newsStatistics;
	}

	public void setNewsStatistics(NewsStatistics newsStatistics) {
		this.newsStatistics = newsStatistics;
	}

}
