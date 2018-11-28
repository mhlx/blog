package me.qyh.blog.plugin.comment.component;

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
import me.qyh.blog.core.dao.ArticleDao;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.plugin.comment.dao.ArticleCommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;
import me.qyh.blog.plugin.comment.service.CommentModuleHandler;
import me.qyh.blog.plugin.comment.vo.LastArticleComment;
import me.qyh.blog.plugin.comment.vo.ModuleCommentCount;

@Component
public class ArticleCommentModuleHandler extends CommentModuleHandler {

	@Autowired
	private LockManager lockManager;
	@Autowired
	private ArticleCommentDao articleCommentDao;
	@Autowired
	private ArticleDao articleDao;

	@Autowired
	private UrlHelper urlHelper;

	@Autowired
	private Messages messages;

	private static final String MODULE_NAME = ArticleService.COMMENT_MODULE_NAME;

	public ArticleCommentModuleHandler() {
		super(new Message("comment.module.article", "文章"), MODULE_NAME);
	}

	@Override
	public void doValidateBeforeInsert(Integer id) throws LogicException {
		Article article = articleDao.selectById(id);
		// 博客不存在
		if (article == null || !Environment.match(article.getSpace()) || !article.isPublished()) {
			throw new LogicException("article.notExists", "文章不存在");
		}
		// 如果私人文章并且没有登录
		if (article.isPrivate()) {
			Environment.doAuthencation();
		}
		if (!article.getAllowComment() && !Environment.hasAuthencated()) {
			throw new LogicException("article.notAllowComment", "文章不允许被评论");
		}
		lockManager.openLock(article.getLockId());
	}

	@Override
	public boolean doValidateBeforeQuery(Integer id) {
		Article article = articleDao.selectById(id);
		if (article == null || !article.isPublished()) {
			return false;
		}
		if (article.isPrivate() && !Environment.hasAuthencated()) {
			return false;
		}
		if (!Environment.match(article.getSpace())) {
			return false;
		}
		lockManager.openLock(article.getLockId());
		lockManager.openLock(article.getSpace().getLockId());
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
	public int queryCommentNum(Space space, boolean queryPrivate) {
		return articleCommentDao.selectTotalCommentCount(space, queryPrivate);
	}

	@Override
	public List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin) {

		User user = Environment.getUser();
		List<Comment> comments = articleCommentDao.selectLastComments(space, limit, queryPrivate, queryAdmin);
		for (Comment comment : comments) {
			LastArticleComment lac = (LastArticleComment) comment;
			if (user == null && lac.getArticle() != null && lac.getArticle().hasLock()) {
				comment.setContent(messages.getMessage(
						Editor.MD.equals(comment.getEditor()) ? PROTECTED_COMMENT_MD : PROTECTED_COMMENT_HTML));
			}
		}
		return comments;
	}

	@Override
	public Optional<String> getUrl(Integer id) {
		Article article = articleDao.selectById(id);
		return article == null ? Optional.empty() : Optional.of(urlHelper.getUrls().getUrl(article));
	}

}
