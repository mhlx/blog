package me.qyh.blog.file.vo;

import java.util.Objects;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.store.FileStore;

/**
 * 文件服务描述
 * 
 * @author Administrator
 *
 */
public class FileStoreBean {

	private int id;
	private String name;

	/**
	 * default
	 */
	public FileStoreBean() {
		super();
	}	

	/**
	 * 文件服务描述构造器
	 * 
	 * @param server
	 *            文件服务
	 */
	public FileStoreBean(FileStore store) {
		this.id = store.id();
		this.name = store.name();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			FileStoreBean fsb = (FileStoreBean) obj;
			return Objects.equals(this.id, fsb.id);
		}
		return false;
	}
}
