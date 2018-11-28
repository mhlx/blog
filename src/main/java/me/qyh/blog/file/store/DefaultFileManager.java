package me.qyh.blog.file.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.plugin.FileStoreRegistry;

/**
 * 默认文件存储管理器
 * 
 * @author Administrator
 *
 */
public class DefaultFileManager implements FileManager, FileStoreRegistry {

	private List<FileStore> stores = new ArrayList<>();
	private Map<Integer, FileStore> storeMap = new HashMap<>();

	@Override
	public Optional<FileStore> getFileStore(int id) {
		return Optional.ofNullable(storeMap.get(id));
	}

	@Override
	public List<FileStore> getAllStores() {
		return Collections.unmodifiableList(stores);
	}

	public void setStores(List<FileStore> stores) {
		this.stores = stores;
		if (!CollectionUtils.isEmpty(stores)) {
			storeMap = stores.stream().collect(Collectors.toMap(FileStore::id, store -> store));
		}
	}

	@Override
	public void addFileStore(FileStore fs) {
		int id = fs.id();
		if (storeMap.containsKey(id)) {
			throw new SystemException("已经存在id为" + id + "的存储器了");
		}
		stores.add(fs);
		storeMap.put(id, fs);
	}

	@Override
	public FileStoreRegistry register(FileStore fileStore) {
		addFileStore(fileStore);
		return this;
	}
}
