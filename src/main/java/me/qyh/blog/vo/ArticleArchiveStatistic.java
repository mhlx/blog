package me.qyh.blog.vo;

import java.io.Serializable;

public class ArticleArchiveStatistic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int year;
	private int month;
	private int count;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
