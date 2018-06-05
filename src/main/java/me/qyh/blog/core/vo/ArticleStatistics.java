/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
