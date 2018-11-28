package me.qyh.blog.plugin.staticfile.vo;

public class StaticFileStatistics {

	private int dirCount;// 文件夹数目
	private int fileCount;// 文件数目
	private long fileSize;// 文件总大小

	public StaticFileStatistics() {
		super();
	}

	public StaticFileStatistics(int dirCount, int fileCount, long fileSize) {
		super();
		this.dirCount = dirCount;
		this.fileCount = fileCount;
		this.fileSize = fileSize;
	}

	public int getDirCount() {
		return dirCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public void setDirCount(int dirCount) {
		this.dirCount = dirCount;
	}

}
