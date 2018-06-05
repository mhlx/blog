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
package me.qyh.blog.plugin.comment.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.NewsDao;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.event.NewsDelEvent;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.plugin.comment.dao.CommentDao;
import me.qyh.blog.plugin.comment.dao.NewsCommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentModuleHandler;
import me.qyh.blog.plugin.comment.vo.ModuleCommentCount;

@Component
public class NewsCommentModuleHandler extends CommentModuleHandler {

	private static final String MODULE_NAME = NewsService.COMMENT_MODULE_TYPE;

	@Autowired
	private CommentDao commentDao;
	@Autowired
	private NewsDao newsDao;
	@Autowired
	private NewsCommentDao newsCommentDao;
	@Autowired
	private UrlHelper urlHelper;

	public NewsCommentModuleHandler() {
		super(new Message("comment.module.news","动态"),MODULE_NAME);
	}

	@Override
	public void doValidateBeforeInsert(Integer id) throws LogicException {
		News news = newsDao.selectById(id);
		if (news == null) {
			throw new LogicException("news.notExists", "动态不存在");
		}
		if (!news.getAllowComment() && !Environment.isLogin()) {
			throw new LogicException("news.notAllowComment", "动态不允许评论");
		}
	}

	@Override
	public boolean doValidateBeforeQuery(Integer id) {
		News news = newsDao.selectById(id);
		if (news == null) {
			return false;
		}
		if (news.getIsPrivate() && !Environment.isLogin()) {
			return false;
		}
		return true;
	}

	@Override
	public Map<Integer, Integer> queryCommentNums(Collection<Integer> ids) {
		List<CommentModule> modules = ids.stream().map(id -> new CommentModule(MODULE_NAME, id))
				.collect(Collectors.toList());
		return commentDao.selectCommentCounts(modules).stream()
				.collect(Collectors.toMap(ModuleCommentCount::getModuleId, ModuleCommentCount::getComments));
	}

	@Override
	public OptionalInt queryCommentNum(Integer id) {
		ModuleCommentCount count = commentDao.selectCommentCount(new CommentModule(MODULE_NAME, id));
		return count == null ? OptionalInt.empty() : OptionalInt.of(count.getComments());
	}

	@Override
	public Map<Integer, Object> getReferences(Collection<Integer> ids) {
		List<News> pages = newsDao.selectByIds(ids);
		return pages.stream().collect(Collectors.toMap(News::getId, p -> p));
	}

	@EventListener
	public void handleNewsEvent(NewsDelEvent event) {
		for (News news : event.getNewsList()) {
			commentDao.deleteByModule(new CommentModule(MODULE_NAME, news.getId()));
		}
	}

	@Override
	public List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin) {
		if (space == null) {
			return newsCommentDao.selectLastComments(limit, queryPrivate, queryAdmin);
		}
		return new ArrayList<>();
	}

	@Override
	public int queryCommentNum(Space space, boolean queryPrivate) {
		if (space == null) {
			return newsCommentDao.selectTotalCommentCount(queryPrivate);
		}
		return 0;
	}

	@Override
	public Optional<String> getUrl(Integer id) {
		News news = newsDao.selectById(id);
		return news == null ? Optional.empty() : Optional.of(urlHelper.getUrls().getUrl(news));
	}

}
