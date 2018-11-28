package me.qyh.blog.core.vo;

import java.io.Serializable;

import me.qyh.blog.core.entity.Space;

/**
 * 
 * @author Administrator
 *
 */
public class ArticleSpaceStatistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Space space;
	private int count;

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
