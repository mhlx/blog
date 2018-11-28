package me.qyh.blog.core.vo;

import java.util.EnumMap;
import java.util.Map;

import me.qyh.blog.core.entity.Article.ArticleStatus;

public class ArticleDetailStatistics extends ArticleStatistics {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<ArticleStatus, Integer> statusCountMap = new EnumMap<>(ArticleStatus.class);

	public ArticleDetailStatistics(ArticleStatistics statistics) {
		super(statistics);
	}

	public Map<ArticleStatus, Integer> getStatusCountMap() {
		return statusCountMap;
	}

	public void setStatusCountMap(Map<ArticleStatus, Integer> statusCountMap) {
		this.statusCountMap = statusCountMap;
	}
}
