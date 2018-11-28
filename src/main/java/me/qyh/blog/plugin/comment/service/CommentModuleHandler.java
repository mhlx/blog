package me.qyh.blog.plugin.comment.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.plugin.comment.dao.CommentDao;
import me.qyh.blog.plugin.comment.entity.Comment;
import me.qyh.blog.plugin.comment.entity.CommentModule;

public abstract class CommentModuleHandler {

	protected static final Message PROTECTED_COMMENT_MD = new Message("comment.protected", "\\*\\*\\*\\*\\*\\*");
	protected static final Message PROTECTED_COMMENT_HTML = new Message("comment.protected", "******");

	@Autowired
	protected CommentDao commentDao;

	// 模块类型
	private final Message name;
	private final String moduleName;

	public CommentModuleHandler(Message name, String moduleName) {
		super();
		this.moduleName = moduleName;
		this.name = name;
	}

	public Message getName() {
		return name;
	}

	public String getModuleName() {
		return moduleName;
	}

	/**
	 * 在插入评论之前做校验
	 * 
	 * @param id
	 *            项目ID
	 * @throws LogicException
	 */
	public abstract void doValidateBeforeInsert(Integer id) throws LogicException;

	/**
	 * 在查询评论前做校验
	 * 
	 * @param id
	 * @return 是否校驗通过
	 */
	public abstract boolean doValidateBeforeQuery(Integer id);

	/**
	 * 查询多个项目的评论数
	 * 
	 * @param ids
	 * @return
	 */
	public abstract Map<Integer, Integer> queryCommentNums(Collection<Integer> ids);

	/**
	 * 查詢某个项目的评论数
	 * 
	 * @param id
	 * @return
	 */
	public abstract OptionalInt queryCommentNum(Integer id);

	/**
	 * 查詢某個空間下所有項目的評論總數
	 * 
	 * @param space
	 *            空间
	 * @param queryPrivate
	 *            是否查询私人项目
	 * @return
	 */
	public abstract int queryCommentNum(Space space, boolean queryPrivate);

	/**
	 * 查询最近的评论
	 * 
	 * @param space
	 *            空间
	 * @param limit
	 *            最大评论数
	 * @param queryPrivate
	 *            是否查询私人项目
	 * @param queryAdmin
	 *            是否查询管理员的回復
	 * @return
	 */
	public abstract List<Comment> queryLastComments(Space space, int limit, boolean queryPrivate, boolean queryAdmin);

	/**
	 * 获取某个项目的访问地址
	 * 
	 * @param id
	 * @return
	 */
	public abstract Optional<String> getUrl(Integer id);

	/**
	 * 删除评论
	 * 
	 * @param id
	 */
	public void deleteComments(Integer id) {
		CommentModule module = new CommentModule(moduleName, id);
		commentDao.deleteByModule(module);
	}
}
