package me.qyh.blog.core.security;

import java.util.ArrayList;
import java.util.HashMap;
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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 用来创建一个{@code AttemptLogger}
 * 
 */
@Component
public class AttemptLoggerManager {

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	private final Map<Integer, List<AttemptLogger>> map = new HashMap<>();

	private final Object lock = new Object();

	/**
	 * <b>创建</b>一个{@code AttemptLogger}
	 * 
	 * @param attemptCount
	 * @param maxAttemptCount
	 * @param sec
	 * @return
	 */
	public AttemptLogger createAttemptLogger(int attemptCount, int maxAttemptCount, int sec) {
		synchronized (lock) {
			List<AttemptLogger> loggers = map.get(sec);
			boolean doSchedule = loggers == null;
			if (doSchedule) {
				loggers = new ArrayList<>(1);
			}
			AttemptLogger logger = new AttemptLogger(attemptCount, maxAttemptCount);
			loggers.add(logger);
			map.put(sec, loggers);
			if (doSchedule) {
				taskScheduler.scheduleWithFixedDelay(() -> {
					synchronized (lock) {
						map.get(sec).forEach(AttemptLogger::clear);
					}
				}, sec * 1000L);
			}
			return logger;
		}
	}
}
