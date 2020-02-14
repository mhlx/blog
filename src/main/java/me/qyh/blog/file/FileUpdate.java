package me.qyh.blog.file;

import javax.validation.constraints.Size;

public class FileUpdate {
	private String content;
	@Path(message = "非法的文件夹路径")
	private String dirPath;
	@Size(max = 255, message = "文件名称不能超过255个字符")
	private String name;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}