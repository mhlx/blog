package me.qyh.blog.plugin.comment.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

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
		Page page = pageDao.selectById(id).orElseThrow(() -> new LogicException("page.user.notExists", "页面不存在"));
		if (!page.getAllowComment() && !Environment.hasAuthencated()) {
			throw new LogicException("page.notAllowComment", "页面不允许评论");
		}
	}

	@Override
	public boolean doValidateBeforeQuery(Integer id) {
		return pageDao.selectById(id).filter(page -> Environment.match(page.getSpace())).isPresent();
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
	public List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin) {
		return pageCommentDao.selectLastComments(space, limit, queryPrivate, queryAdmin);
	}

	@Override
	public int queryCommentNum(Space space, boolean queryPrivate) {
		return pageCommentDao.selectTotalCommentCount(space, queryPrivate);
	}

	@Override
	public Optional<String> getUrl(Integer id) {
		return pageDao.selectById(id).map(page -> urlHelper.getUrls().getUrl(page));
	}

}
