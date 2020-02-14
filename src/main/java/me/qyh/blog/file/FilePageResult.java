package me.qyh.blog.file;

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.vo.PageResult;

public class FilePageResult extends PageResult<FileInfo> {

	private List<FileInfo> paths = new ArrayList<>();

	public FilePageResult() {
		super();
	}

	public FilePageResult(FileQueryParam param, int totalRow, List<FileInfo> datas) {
		super(param, totalRow, datas);
	}

	public FilePageResult(FileQueryParam param, int totalRow, List<FileInfo> datas, List<FileInfo> paths) {
		super(param, totalRow, datas);
		this.paths = paths;
	}

	public List<FileInfo> getPaths() {
		return paths;
	}

	public void setPaths(List<FileInfo> paths) {
		this.paths = paths;
	}

}
