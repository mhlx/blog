package me.qyh.blog.core.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ArticleStatistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timestamp lastModifyDate;// 最后修改日期
	private Timestamp lastPubDate;
	private int totalHits;// 点击总数
	private int totalArticles;// 文章总数

	/**
	 * 当查询默认空间时，同时将显示所有空间下的文章数
	 * 
	 * @since 5.5.4
	 */
	private List<ArticleSpaceStatistics> spaceStatisticsList = new ArrayList<>();

	ArticleStatistics() {
		super();
	}

	ArticleStatistics(ArticleStatistics statistics) {
		super();
		this.lastModifyDate = statistics.lastModifyDate;
		this.lastPubDate = statistics.lastPubDate;
		this.totalHits = statistics.totalHits;
		this.totalArticles = statistics.totalArticles;
		this.spaceStatisticsList = statistics.spaceStatisticsList;
	}

	public Timestamp getLastModifyDate() {
		return lastModifyDate;
	}

	public void setLastModifyDate(Timestamp lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public Timestamp getLastPubDate() {
		return lastPubDate;
	}

	public void setLastPubDate(Timestamp lastPubDate) {
		this.lastPubDate = lastPubDate;
	}

	public int getTotalArticles() {
		return totalArticles;
	}

	public void setTotalArticles(int totalArticles) {
		this.totalArticles = totalArticles;
	}

	public List<ArticleSpaceStatistics> getSpaceStatisticsList() {
		return spaceStatisticsList;
	}

	public void setSpaceStatisticsList(List<ArticleSpaceStatistics> spaceStatisticsList) {
		this.spaceStatisticsList = spaceStatisticsList;
	}
}
