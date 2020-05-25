package me.qyh.blog.file;

import java.time.LocalDateTime;
import java.util.Arrays;

public class FileInfo {

	private String path;
	private String name;
	private String ext;
	private boolean dir;
	private boolean editable;
	private String smallThumbPath;
	private String middleThumbPath;
	private String largeThumbPath;
	private LocalDateTime lastModify;
	private boolean isPrivate;
	private boolean isProtected;
	private String url;
	private String smallThumbUrl;
	private String middleThumbUrl;
	private String largeThumbUrl;

	public FileInfo() {
		super();
	}

	public FileInfo(FileInfo fi) {
		this.path = fi.path;
		this.name = fi.name;
		this.ext = fi.ext;
		this.dir = fi.dir;
		this.editable = fi.editable;
		this.lastModify = fi.lastModify;
		this.isPrivate = fi.isPrivate;
		this.isProtected = fi.isProtected;
		this.smallThumbPath = fi.smallThumbPath;
		this.middleThumbPath = fi.middleThumbPath;
		this.largeThumbPath = fi.largeThumbPath;
		this.smallThumbUrl = fi.smallThumbUrl;
		this.middleThumbUrl = fi.middleThumbUrl;
		this.largeThumbUrl = fi.largeThumbUrl;
		this.url = fi.url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public String getSmallThumbPath() {
		return smallThumbPath;
	}

	public void setSmallThumbPath(String smallThumbPath) {
		this.smallThumbPath = smallThumbPath;
	}

	public String getMiddleThumbPath() {
		return middleThumbPath;
	}

	public void setMiddleThumbPath(String middleThumbPath) {
		this.middleThumbPath = middleThumbPath;
	}

	public String getLargeThumbPath() {
		return largeThumbPath;
	}

	public void setLargeThumbPath(String largeThumbPath) {
		this.largeThumbPath = largeThumbPath;
	}

	public LocalDateTime getLastModify() {
		return lastModify;
	}

	public void setLastModify(LocalDateTime lastModify) {
		this.lastModify = lastModify;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSmallThumbUrl() {
		return smallThumbUrl;
	}

	public void setSmallThumbUrl(String smallThumbUrl) {
		this.smallThumbUrl = smallThumbUrl;
	}

	public String getMiddleThumbUrl() {
		return middleThumbUrl;
	}

	public void setMiddleThumbUrl(String middleThumbUrl) {
		this.middleThumbUrl = middleThumbUrl;
	}

	public String getLargeThumbUrl() {
		return largeThumbUrl;
	}

	public void setLargeThumbUrl(String largeThumbUrl) {
		this.largeThumbUrl = largeThumbUrl;
	}

	/**
	 * 判斷是否匹配某些后缀
	 * 
	 * @param ext
	 * @return
	 */
	public boolean is(String... exts) {
		if (exts == null || exts.length == 0) {
			return false;
		}
		if (dir) {
			return false;
		}
		if (this.ext == null || this.ext.isEmpty()) {
			return ext == null || ext.isEmpty();
		}
		return Arrays.stream(exts).anyMatch(this.ext::equalsIgnoreCase);
	}
}
