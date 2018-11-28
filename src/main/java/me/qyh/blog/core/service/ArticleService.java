package me.qyh.blog.core.service;

import java.util.List;
import java.util.Optional;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Editor;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.AuthencationException;
import me.qyh.blog.core.vo.ArticleArchive;
import me.qyh.blog.core.vo.ArticleArchivePageQueryParam;
import me.qyh.blog.core.vo.ArticleDetailStatistics;
import me.qyh.blog.core.vo.ArticleNav;
import me.qyh.blog.core.vo.ArticleQueryParam;
import me.qyh.blog.core.vo.ArticleStatistics;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.TagCount;

/**
 * 文章服务
 * 
 * @author Administrator
 *
 */
public interface ArticleService {

	final String COMMENT_MODULE_NAME = "article";

	/**
	 * 获取一篇可以被访问的文章
	 * 
	 * @param idOrAlias
	 *            id或者文章别名
	 * @throws AuthencationException
	 *             如果访问了私人博客但是没有登录
	 * @return
	 */
	Optional<Article> getArticleForView(String idOrAlias);

	/**
	 * 获取一篇可以被编辑的文章
	 * 
	 * @param id
	 *            文章id
	 * @return 文章
	 */
	Optional<Article> getArticleForEdit(Integer id);

	/**
	 * 分页查询文章
	 * 
	 * @param param
	 *            查询参数
	 * @return 文章分页对象
	 */
	PageResult<Article> queryArticle(ArticleQueryParam param);

	/**
	 * 插入|更新 文章
	 * 
	 * <b>自动保存的文章将会被设置为DRAFT</b>
	 * 
	 * @param article
	 *            文章
	 * @return 插入后的文章
	 * @throws LogicException
	 */
	Article writeArticle(Article article) throws LogicException;

	/**
	 * 删除博客
	 * 
	 * @param id
	 *            文章id
	 * @throws LogicException
	 */
	void deleteArticle(Integer id) throws LogicException;

	/**
	 * 增加文章点击数
	 * 
	 * @param id
	 *            文章id
	 * @return 当前点击数
	 */
	void hit(Integer id);

	/**
	 * 上一篇，下一篇文章
	 * 
	 * @param idOrAlias
	 *            文章的id或者别名
	 * @param queryLock
	 *            是否查询被锁保护的文章
	 * @return 当前文章的上一篇下一篇，如果都没有，返回null
	 */
	Optional<ArticleNav> getArticleNav(String idOrAlias, boolean queryLock);

	/**
	 * 获取上一篇，下一篇文章
	 * <p>
	 * <b>仅供模板调用！！！</b>
	 * </p>
	 * 
	 * @param article
	 *            当前文章
	 * @param queryLock
	 * @return
	 */
	Optional<ArticleNav> getArticleNav(Article article, boolean queryLock);

	/**
	 * 查询<b>当前空间</b>被文章引用的标签数量
	 * <p>
	 * <b>用于DataTag</b>
	 * </p>
	 * 
	 * @return 标签集
	 */
	List<TagCount> queryTags();

	/**
	 * 用来处理预览文章，比如将markdown转化为html
	 * 
	 * @param article
	 *            预览文章
	 */
	String createPreviewContent(Editor editor, String content);

	/**
	 * 更新文章
	 * 
	 * @param article
	 * @return
	 * @throws LogicException
	 */
	Article updateArticle(Article article) throws LogicException;

	/**
	 * 查询文章归档
	 * 
	 * @param
	 * @since 7.0
	 */
	PageResult<ArticleArchive> queryArticleArchives(ArticleArchivePageQueryParam param);

	/**
	 * 统计某个空间下的文章
	 * 
	 * @param space
	 * @return
	 */
	ArticleDetailStatistics queryArticleDetailStatistics(Space space);

	/**
	 * 统计所有的文章
	 * 
	 * @return
	 */
	ArticleStatistics queryArticleStatistics();

	/**
	 * 更改文章的状态
	 * 
	 * @since 7.0
	 * @param id
	 * @param status
	 * @throws LogicException
	 */
	void changeStatus(Integer id, ArticleStatus status) throws LogicException;

}
