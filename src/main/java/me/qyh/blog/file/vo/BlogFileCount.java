package me.qyh.blog.file.vo;

import me.qyh.blog.file.entity.BlogFile.BlogFileType;

/**
 * 文件类型对应的文件数量
 * 
 * @author Administrator
 *
 */
public class BlogFileCount {

	private BlogFileType type;
	private int count;

	public BlogFileType getType() {
		return type;
	}

	public void setType(BlogFileType type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
