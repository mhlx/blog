package me.qyh.blog.core.plugin;

import me.qyh.blog.file.store.FileStore;

public interface FileStoreRegistry {
	
	FileStoreRegistry register(FileStore fileStore);

}
