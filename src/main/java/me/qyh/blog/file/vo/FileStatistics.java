package me.qyh.blog.file.vo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import me.qyh.blog.file.entity.BlogFile.BlogFileType;

public class FileStatistics {
	private Map<BlogFileType, Integer> typeCountMap = new EnumMap<>(BlogFileType.class);
	private Map<FileStoreBean, FileCount> storeCountMap = new HashMap<>();

	public Map<BlogFileType, Integer> getTypeCountMap() {
		return typeCountMap;
	}

	public void setTypeCountMap(Map<BlogFileType, Integer> typeCountMap) {
		this.typeCountMap = typeCountMap;
	}

	public Map<FileStoreBean, FileCount> getStoreCountMap() {
		return storeCountMap;
	}

	public void setStoreCountMap(Map<FileStoreBean, FileCount> storeCountMap) {
		this.storeCountMap = storeCountMap;
	}

}
