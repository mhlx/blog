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
package me.qyh.blog.plugin.hitstory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.event.ArticleDelEvent;
import me.qyh.blog.core.event.ArticleUpdateEvent;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.service.ArticleHitHandler;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.SerializationUtils;
import me.qyh.blog.core.util.Times;

/**
 * 将最近访问的文章纪录在内存中
 * <p>
 * <b>可能文章状态可能会变更，所以实际返回的数量可能小于<i>max</i></b> <br>
 * 
 * <p>
 * </p>
 * 
 * @author Administrator
 *
 */
public class HitsHistoryLogger implements InitializingBean, ArticleHitHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(HitsHistoryLogger.class);

	private final int max;
	private Map<Integer, HitsHistory> articles;
	private final StampedLock lock = new StampedLock();

	/**
	 * 应用关闭时当前访问的文章存入文件中
	 */
	private final Path sdfile = Constants.DAT_DIR.resolve("sync_articles_viewd.dat");

	public HitsHistoryLogger(int max) {
		if (max < 0) {
			throw new SystemException("max必须大于0");
		}
		this.max = max;

		articles = new LinkedHashMap<>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<Integer, HitsHistory> eldest) {
				return size() > max;
			}

		};
	}

	public HitsHistoryLogger() {
		this(10);
	}

	public List<HitsHistory> getHistory(int num) {
		long stamp = lock.tryOptimisticRead();
		List<HitsHistory> result = getCurrentViewed(num);
		if (!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				result = getCurrentViewed(num);
			} finally {
				lock.unlockRead(stamp);
			}
		}
		return result;
	}

	private List<HitsHistory> getCurrentViewed(int num) {
		List<HitsHistory> result = new ArrayList<>(articles.values());
		if (!result.isEmpty()) {
			Collections.reverse(result);
			int finalNum = Math.min(num, max);
			if (result.size() > finalNum) {
				result = result.subList(0, finalNum);
			}
		}
		return result;
	}

	@TransactionalEventListener
	public void handleArticleEvent(ArticleDelEvent evt) {
		long stamp = lock.writeLock();
		try {
			evt.getArticles().forEach(art -> articles.remove(art.getId()));
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@TransactionalEventListener
	public void handleArticleEvent(ArticleUpdateEvent evt) {
		long stamp = lock.writeLock();
		try {
			Article art = evt.getNewArticle();
			boolean valid = art.isPublished() && !art.isPrivate();
			if (!valid) {
				articles.remove(art.getId());
			} else {
				HitsHistory rva = articles.get(art.getId());
				if (rva != null) {
					articles.replace(art.getId(), new HitsHistory(art, rva.getIp(), rva.getTime()));
				}
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@EventListener
	public void handleContextCloseEvent(ContextClosedEvent evt) throws IOException {
		if (evt.getApplicationContext().getParent() != null) {
			return;
		}
		if (!articles.isEmpty()) {
			SerializationUtils.serialize(new LinkedHashMap<>(articles), sdfile);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (FileUtils.exists(sdfile)) {
			try {
				this.articles.putAll(SerializationUtils.deserialize(sdfile));
			} catch (Exception e) {
				LOGGER.warn("反序列化文件" + sdfile + "失败：" + e.getMessage(), e);
			} finally {
				if (!FileUtils.deleteQuietly(sdfile)) {
					LOGGER.warn("删除文件{}失败", sdfile);
				}
			}
		}
	}

	@Override
	public void hit(Article article) {
		LocalDateTime time = Times.now();
		long stamp = lock.writeLock();
		try {
			articles.remove(article.getId());
			articles.put(article.getId(), new HitsHistory(article, Environment.getIP(), time));
		} finally {
			lock.unlockWrite(stamp);
		}
	}

}
