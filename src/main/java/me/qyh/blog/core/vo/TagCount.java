package me.qyh.blog.core.vo;

import me.qyh.blog.core.entity.Tag;

/**
 * 标签对应的引用数量
 * 
 * @author Administrator
 *
 */
public class TagCount {

	private Tag tag;
	private int count;

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
