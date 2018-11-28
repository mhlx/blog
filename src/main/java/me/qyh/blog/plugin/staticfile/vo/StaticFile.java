package me.qyh.blog.plugin.staticfile.vo;

public class StaticFile {

	private String path;
	private long size;
	private String name;
	private String ext;
	private boolean dir;
	private boolean editable;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public boolean isDir() {
		return dir;
	}

	public void setDir(boolean dir) {
		this.dir = dir;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * 判斷是否匹配某后缀
	 * 
	 * @param ext
	 * @return
	 */
	public boolean is(String ext) {
		if (dir) {
			return false;
		}
		if (this.ext == null || this.ext.isEmpty()) {
			return ext == null || ext.isEmpty();
		}
		return this.ext.equalsIgnoreCase(ext);
	}
}
