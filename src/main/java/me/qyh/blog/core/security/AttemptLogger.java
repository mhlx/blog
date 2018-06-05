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
package me.qyh.blog.core.security;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import me.qyh.blog.core.exception.SystemException;

/**
 * 用来判断是否需要输入验证码
 * <p>
 * 当某个ip尝试此时达到attemptCount的情况下，如果该ip继续尝试，则需要输入验证码<br>
 * 当尝试总数达到maxAttemptCount的情况下，如果有任何ip继续尝试，则需要输入验证码<br>
 * </p>
 * 基本用法:
 * 
 * <pre>
 * AttemptLogger logger = attemptLoggerManager.createAttemptLogger();
 * if(logger.log(ip)){
 *   //判断验证码是否正确
 * }
 * 
 * reach(ip) 
 * //判断某个ip是否需要输入验证码
 * </pre>
 * 
 * @author Administrator
 * @see AttemptLoggerManager
 */
public class AttemptLogger {

	private final int attemptCount;
	private final int maxAttemptCount;
	private final Map<String, AtomicInteger> map = new ConcurrentHashMap<>();
	private final AtomicInteger maxAttemptCounter;

	AttemptLogger(int attemptCount, int maxAttemptCount) {
		super();
		if (attemptCount < 1) {
			throw new SystemException("尝试次数不能小于1");
		}
		this.attemptCount = attemptCount;
		this.maxAttemptCount = maxAttemptCount;
		this.maxAttemptCounter = new AtomicInteger(0);
	}

	/**
	 * 尝试，次数+1
	 * 
	 * @param t
	 * @return 如果返回true，则说明达到阈值
	 */
	public boolean log(String t) {
		Objects.requireNonNull(t);
		BooleanHolder holder = new BooleanHolder(true);
		map.compute(t, (k, v) -> {
			if (v == null && maxAttemptCounter.get() < maxAttemptCount) {
				v = new AtomicInteger();
			}
			if (v != null) {
				holder.value = add(v);
			}
			return v;
		});
		return holder.value;
	}

	/**
	 * 是否达到阈值
	 * 
	 * @param t
	 * @return
	 */
	public boolean reach(String t) {
		if (maxAttemptCounter.get() == maxAttemptCount) {
			return true;
		}
		AtomicInteger counter = map.get(t);
		return counter != null && counter.get() == attemptCount;
	}

	private boolean add(AtomicInteger v) {
		int count = v.get();
		if (count == attemptCount) {
			return true;
		}
		for (;;) {
			int maxCount = maxAttemptCounter.get();
			if (maxCount == maxAttemptCount) {
				return true;
			}
			if (maxAttemptCounter.compareAndSet(maxCount, maxCount + 1)) {
				v.incrementAndGet();
				return false;
			}
		}
	}

	/**
	 * 移除一个记录
	 * 
	 * @param t
	 */
	public void remove(String t) {
		map.computeIfPresent(t, (k, v) -> {
			maxAttemptCounter.addAndGet(-v.get());
			return null;
		});
	}

	void clear() {
		map.keySet().forEach(this::remove);
	}

	private final class BooleanHolder {
		private boolean value;

		BooleanHolder(boolean value) {
			super();
			this.value = value;
		}
	}
}