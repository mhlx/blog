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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.plugin.comment.dao.PageCommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentModuleHandler;
import me.qyh.blog.plugin.comment.vo.ModuleCommentCount;
import me.qyh.blog.template.dao.PageDao;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.service.TemplateService;

@Component
public class PageCommentModuleHandler extends CommentModuleHandler {

	private static final String MODULE_NAME = TemplateService.COMMENT_MODULE_NAME;

	@Autowired
	private PageDao pageDao;
	@Autowired
	private PageCommentDao pageCommentDao;
	@Autowired
	private UrlHelper urlHelper;

	public PageCommentModuleHandler() {
		super(new Message("comment.module.page", "页面"), MODULE_NAME);
	}

	@Override
	public void doValidateBeforeInsert(Integer id) throws LogicException {
		Page page = pageDao.selectById(id);
		if (page == null) {
			throw new LogicException("page.user.notExists", "页面不存在");
		}
		if (!page.getAllowComment() && !Environment.isLogin()) {
			throw new LogicException("page.notAllowComment", "页面不允许评论");
		}
	}

	@Override
	public boolean doValidateBeforeQuery(Integer id) {
		Page page = pageDao.selectById(id);
		return page != null && Environment.match(page.getSpace());
	}

	@Override
	public Map<Integer, Integer> queryCommentNums(Collection<Integer> ids) {
		return new HashMap<>();
	}

	@Override
	public OptionalInt queryCommentNum(Integer id) {
		ModuleCommentCount count = commentDao.selectCommentCount(new CommentModule(MODULE_NAME, id));
		return count == null ? OptionalInt.empty() : OptionalInt.of(count.getComments());
	}

	@Override
	public Map<Integer, Object> getReferences(Collection<Integer> ids) {
		List<Page> pages = pageDao.selectSimpleByIds(ids);
		return pages.stream().collect(Collectors.toMap(Page::getId, p -> p));
	}

	@Override
	public List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin) {
		return pageCommentDao.selectLastComments(space, limit, queryPrivate, queryAdmin);
	}

	@Override
	public int queryCommentNum(Space space, boolean queryPrivate) {
		return pageCommentDao.selectTotalCommentCount(space, queryPrivate);
	}

	@Override
	public Optional<String> getUrl(Integer id) {
		Page page = pageDao.selectById(id);
		return Optional.ofNullable(page == null ? null : urlHelper.getUrls().getUrl(page));
	}

}
