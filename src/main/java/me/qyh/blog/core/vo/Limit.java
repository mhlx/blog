package me.qyh.blog.core.vo;

import java.util.concurrent.TimeUnit;

/**
 * 在指定时间内最多能执行limit次操作
 * 
 * @author Administrator
 *
 */
public class Limit {

	private int count;
	private long time;
	private TimeUnit unit;

	/**
	 * default
	 */
	public Limit() {
		super();
	}

	/**
	 * 
	 * @param count
	 *            最大数目
	 * @param time
	 *            时间
	 * @param unit
	 *            时间单位
	 */
	public Limit(int count, long time, TimeUnit unit) {
		this.count = count;
		this.time = time;
		this.unit = unit;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	public long toMill() {
		return unit.toMillis(time);
	}
}
