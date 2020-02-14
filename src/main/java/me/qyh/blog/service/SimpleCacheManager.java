package me.qyh.blog.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SimpleCacheManager {

	private final Map<String, SimpleCache<?>> map = new ConcurrentHashMap<>();
	private static final SimpleCacheManager INSTANCE = new SimpleCacheManager();

	static SimpleCacheManager get() {
		return INSTANCE;
	}

	public class SimpleCache<T> {
		private ConcurrentHashMap<Integer, T> map = new ConcurrentHashMap<>();

		public void put(Integer id, T t) {
			map.put(id, t);
		}

		public T get(Integer id) {
			return map.get(id);
		}

		public void remove(Integer id) {
			map.remove(id);
		}

		public void clear() {
			map.clear();
		}

		public Collection<T> getAll() {
			return map.values();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> SimpleCache<T> getCache(String name) {
		return (SimpleCache<T>) map.computeIfAbsent(name, n -> new SimpleCache<T>());
	}
}
