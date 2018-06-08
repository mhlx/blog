/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.plugin.cachehits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.BaseEntity;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.service.impl.HitsStrategy;
import me.qyh.blog.core.service.impl.Transactions;

/**
 * 将点击数缓存起来，每隔一定的时间刷入数据库
 * <p>
 * <b>由于缓存原因，根据点击量查询无法实时的反应当前结果</b>
 * </p>
 * 
 * @author Administrator
 *
 */
abstract class CacheableHitsStrategy<E extends BaseEntity> implements HitsStrategy<E>, InitializingBean {

	@Autowired
	private PlatformTransactionManager transactionManager;
	@Autowired
	private TaskScheduler taskScheduler;
	/**
	 * 存储所有的点击数
	 */
	protected final Map<Integer, HitsHandler<E>> hitsMap = new ConcurrentHashMap<>();

	/**
	 * 储存待刷新的点击数
	 */
	protected final Map<Integer, Boolean> flushMap = new ConcurrentHashMap<>();

	/**
	 * 如果该项为true，那么在flush之前，相同的ip点击只算一次点击量
	 * <p>
	 * 例如我点击一次增加了一次点击量，一分钟后flush，那么我在这一分钟内(ip的不变的情况下)，无论我点击了多少次，都只算一次
	 * </p>
	 */
	private final boolean cacheIp;

	/**
	 * 最多保存的ip数，如果达到或超过该数目，将会立即更新
	 */
	private final int maxIps;

	private final int flushSec;

	public CacheableHitsStrategy(boolean cacheIp, int maxIps, int flushSec) {
		if (maxIps < 0) {
			throw new SystemException("maxIps不能小于0");
		}
		if (flushSec < 0) {
			throw new SystemException("flushSec不能小于0");
		}
		this.maxIps = maxIps;
		this.cacheIp = cacheIp;
		this.flushSec = flushSec;
	}

	@Override
	public void hit(E e) {
		// increase
		hitsMap.computeIfAbsent(e.getId(),
				k -> cacheIp ? new IPBasedHitsHandler(getHits(e), maxIps) : new DefaultHitsHandler(getHits(e))).hit(e);
		flushMap.putIfAbsent(e.getId(), Boolean.TRUE);
	}

	protected abstract int getHits(E e);

	private synchronized void doFlush(List<HitsWrapper> wrappers, boolean contextClose) {
		// 得到当前的实时点击数
		Map<Integer, Integer> hitsMap = wrappers.stream().filter(wrapper -> wrapper.hitsHandler != null)
				.collect(Collectors.toMap(wrapper -> wrapper.id, wrapper -> wrapper.hitsHandler.getHits()));
		if (!hitsMap.isEmpty()) {

			Transactions.executeInTransaction(transactionManager, status -> {
				doFlush(hitsMap, contextClose);
			});
		}
	}

	protected abstract void doFlush(Map<Integer, Integer> hitsMap, boolean contextClose);

	private final class HitsWrapper {
		private final Integer id;
		private final HitsHandler<E> hitsHandler;

		HitsWrapper(Integer id, HitsHandler<E> hitsHandler) {
			super();
			this.id = id;
			this.hitsHandler = hitsHandler;
		}
	}

	private interface HitsHandler<E> {
		void hit(E e);

		int getHits();
	}

	private final class DefaultHitsHandler implements HitsHandler<E> {

		private final LongAdder adder;

		private DefaultHitsHandler(int init) {
			adder = new LongAdder();
			adder.add(init);
		}

		@Override
		public void hit(E e) {
			adder.increment();
		}

		@Override
		public int getHits() {
			return adder.intValue();
		}
	}

	private final class IPBasedHitsHandler implements HitsHandler<E> {
		private final Map<String, Boolean> ips = new ConcurrentHashMap<>();
		private final LongAdder adder;
		private final AtomicInteger ipNums;

		private IPBasedHitsHandler(int init, int maxIps) {
			adder = new LongAdder();
			adder.add(init);
			this.ipNums = new AtomicInteger(maxIps);
		}

		@Override
		public void hit(E e) {
			String ip = Environment.getIP();
			if (ip != null && ips.putIfAbsent(ip, Boolean.TRUE) == null) {
				adder.increment();
				if (ipNums.decrementAndGet() == 0) {
					Integer id = e.getId();
					if (flushMap.remove(id) != null) {
						doFlush(List.of(new HitsWrapper(id, hitsMap.remove(id))), false);
					}
				}
			}
		}

		@Override
		public int getHits() {
			return adder.intValue();
		}
	}

	private void flush() {
		flush(false);
	}

	private void flush(boolean contextClose) {
		if (!flushMap.isEmpty()) {
			List<HitsWrapper> wrappers = new ArrayList<>();
			for (Iterator<Entry<Integer, Boolean>> iter = flushMap.entrySet().iterator(); iter.hasNext();) {
				Entry<Integer, Boolean> entry = iter.next();
				Integer key = entry.getKey();

				if (flushMap.remove(key) != null) {
					wrappers.add(new HitsWrapper(key, hitsMap.remove(key)));
				}
			}
			doFlush(wrappers, contextClose);
		}
	}

	@EventListener
	public void handleContextEvent(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() != null) {
			return;
		}
		flush(true);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		taskScheduler.scheduleAtFixedRate(this::flush, flushSec * 1000L);
	}
}