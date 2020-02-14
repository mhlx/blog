package me.qyh.blog.file;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class FileCreate {

	public enum Type {
		DIR, FILE
	}

	@NotNull(message = "需要创建的文件类型不能为空")
	private Type type;
	@NotBlank(message = "需要创建文件的路径不能为空")
	@Path(message = "非法的文件路径")
	private String path;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
