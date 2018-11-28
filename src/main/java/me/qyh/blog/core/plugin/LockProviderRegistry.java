package me.qyh.blog.core.plugin;

import me.qyh.blog.core.service.LockProvider;

public interface LockProviderRegistry {

	LockProviderRegistry register(LockProvider provider);

}
