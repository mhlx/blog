package me.qyh.blog.file.vo;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class BlogFileUpload {

	private List<MultipartFile> files;
	private Integer parent;
	private Integer store;

	public List<MultipartFile> getFiles() {
		return files;
	}

	public void setFiles(List<MultipartFile> files) {
		this.files = files;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public Integer getStore() {
		return store;
	}

	public void setStore(Integer store) {
		this.store = store;
	}

}
