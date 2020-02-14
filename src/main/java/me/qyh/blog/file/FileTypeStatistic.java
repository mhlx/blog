package me.qyh.blog.file;

public class FileTypeStatistic {

	private final String type;
	private final long size;
	private final long count;

	FileTypeStatistic(String type, long size, long count) {
		super();
		this.type = type;
		this.size = size;
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public long getSize() {
		return size;
	}

	public long getCount() {
		return count;
	}

}
