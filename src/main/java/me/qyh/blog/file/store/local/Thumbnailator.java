/*
 * Copyright 2018 qyh.me
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
package me.qyh.blog.file.store.local;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.store.ImageHelper;
import me.qyh.blog.file.store.Resize;

public class Thumbnailator {

	@Autowired
	private ImageHelper imageHelper;

	private final Semaphore semaphore;
	private final Map<String, Resizer> resizeMap = new ConcurrentHashMap<>();

	public Thumbnailator(int semaphoreNum) {
		super();
		this.semaphore = new Semaphore(semaphoreNum);
	}

	public Thumbnailator() {
		this(5);
	}

	public void doResize(Path local, Resize resize, Path thumb) throws IOException {
		if (FileUtils.exists(thumb)) {
			return;
		}
		String resizeKey = local.toString() + '@' + resize.toString();
		if (resizeMap.putIfAbsent(resizeKey, new Resizer(thumb, local, resize)) == null) {
			try {
				resizeMap.get(resizeKey).resize();
			} finally {
				resizeMap.remove(resizeKey);
			}
		} else {
			Resizer resizer = resizeMap.get(resizeKey);
			if (resizer != null) {
				resizer.resize();
			}
		}
	}

	private final class Resizer {

		private final AtomicBoolean resized = new AtomicBoolean(false);
		private final CountDownLatch latch = new CountDownLatch(1);
		private final Path thumb;
		private final Path local;
		private final Resize resize;

		Resizer(Path thumb, Path local, Resize resize) {
			super();
			this.thumb = thumb;
			this.local = local;
			this.resize = resize;
		}

		void resize() throws IOException {
			if (resized.compareAndSet(false, true)) {
				try {
					semaphore.acquire();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SystemException(e.getMessage(), e);
				}
				try {
					FileUtils.forceMkdir(thumb.getParent());
					imageHelper.resize(resize, local, thumb);
				} finally {
					semaphore.release();
					latch.countDown();
				}
			} else {
				try {
					latch.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SystemException(e.getMessage(), e);
				}
			}
		}
	}

	protected final ImageHelper getImageHelper() {
		return imageHelper;
	}

	protected final boolean supportWebp() {
		return imageHelper.supportWebp();
	}

}
