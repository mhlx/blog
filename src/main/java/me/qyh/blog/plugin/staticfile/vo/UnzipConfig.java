package me.qyh.blog.plugin.staticfile.vo;

public class UnzipConfig {

	private String encoding;
	private boolean deleteAfterSuccessUnzip;
	private String path;

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isDeleteAfterSuccessUnzip() {
		return deleteAfterSuccessUnzip;
	}

	public void setDeleteAfterSuccessUnzip(boolean deleteAfterSuccessUnzip) {
		this.deleteAfterSuccessUnzip = deleteAfterSuccessUnzip;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
