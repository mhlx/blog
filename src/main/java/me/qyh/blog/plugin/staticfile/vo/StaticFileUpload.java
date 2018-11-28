package me.qyh.blog.plugin.staticfile.vo;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class StaticFileUpload {
	
	private String path;
	private List<MultipartFile> files;
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<MultipartFile> getFiles() {
		return files;
	}
	public void setFiles(List<MultipartFile> files) {
		this.files = files;
	}

}
