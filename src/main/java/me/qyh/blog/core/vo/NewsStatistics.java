package me.qyh.blog.core.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class NewsStatistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timestamp lastModify;
	private Timestamp lastWrite;
	private int total;// 动态总数
	private int totalHits;// 点击总数

	NewsStatistics() {
		super();
	}

	NewsStatistics(NewsStatistics statistics) {
		super();
		this.lastModify = statistics.lastModify;
		this.lastWrite = statistics.lastWrite;
		this.total = statistics.total;
	}

	public Timestamp getLastModify() {
		return lastModify;
	}

	public void setLastModify(Timestamp lastModify) {
		this.lastModify = lastModify;
	}

	public Timestamp getLastWrite() {
		return lastWrite;
	}

	public void setLastWrite(Timestamp lastWrite) {
		this.lastWrite = lastWrite;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

}
