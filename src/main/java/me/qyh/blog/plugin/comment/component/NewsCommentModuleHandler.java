package me.qyh.blog.plugin.comment.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.NewsDao;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.plugin.comment.dao.NewsCommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentModuleHandler;
import me.qyh.blog.plugin.comment.vo.LastNewsComment;
import me.qyh.blog.plugin.comment.vo.ModuleCommentCount;

@Component
public class NewsCommentModuleHandler extends CommentModuleHandler {

	private static final String MODULE_NAME = NewsService.COMMENT_MODULE_NAME;

	@Autowired
	private NewsDao newsDao;
	@Autowired
	private NewsCommentDao newsCommentDao;
	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private LockManager lockManager;
	@Autowired
	private Messages messages;

	public NewsCommentModuleHandler() {
		super(new Message("comment.module.news", "动态"), MODULE_NAME);
	}

	@Override
	public void doValidateBeforeInsert(Integer id) throws LogicException {
		News news = newsDao.selectById(id).orElseThrow(() -> new LogicException("news.notExists", "动态不存在"));
		if (news.getIsPrivate()) {
			Environment.doAuthencation();
		}
		if (!news.getAllowComment() && !Environment.hasAuthencated()) {
			throw new LogicException("news.notAllowComment", "动态不允许评论");
		}
		lockManager.openLock(news.getLockId());
	}

	@Override
	public boolean doValidateBeforeQuery(Integer id) {
		Optional<News> op = newsDao.selectById(id);
		if (op.isEmpty()) {
			return false;
		}
		News news = op.get();
		if (news.getIsPrivate() && !Environment.hasAuthencated()) {
			return false;
		}
		lockManager.openLock(news.getLockId());
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
	public List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin) {
		if (space == null) {
			List<Comment> comments = newsCommentDao.selectLastComments(limit, queryPrivate, queryAdmin);
			for (Comment comment : comments) {
				LastNewsComment lnc = (LastNewsComment) comment;
				if (!Environment.hasAuthencated() && lnc.getNews() != null && lnc.getNews().hasLock()) {
					comment.setContent(messages.getMessage(
							Editor.MD.equals(comment.getEditor()) ? PROTECTED_COMMENT_MD : PROTECTED_COMMENT_HTML));
				}
			}
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
		return newsDao.selectById(id).map(news -> urlHelper.getUrls().getUrl(news));
	}

}
