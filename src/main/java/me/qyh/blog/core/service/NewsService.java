package me.qyh.blog.core.service;

import java.util.List;
import java.util.Optional;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.vo.NewsArchive;
import me.qyh.blog.core.vo.NewsArchivePageQueryParam;
import me.qyh.blog.core.vo.NewsNav;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.core.vo.NewsStatistics;
import me.qyh.blog.core.vo.PageResult;

public interface NewsService {

	String COMMENT_MODULE_NAME = "news";

	/**
	 * 分页查询动态
	 * 
	 * @param param
	 * @return
	 */
	PageResult<News> queryNews(NewsQueryParam param);

	/**
	 * 保存动态
	 * 
	 * @param news
	 * @throws LogicException
	 */
	void saveNews(News news) throws LogicException;

	/**
	 * 更新动态
	 * 
	 * @param news
	 * @throws LogicException
	 */
	void updateNews(News news) throws LogicException;

	/**
	 * 查询指定的动态
	 * 
	 * @param id
	 * @return
	 * @throws LogicException
	 */
	Optional<News> getNews(Integer id);

	/**
	 * 查询指定的动态(不会转化内容)
	 * 
	 * @param id
	 * @return
	 */
	Optional<News> getNewsForEdit(Integer id);

	/**
	 * 删除指的动态
	 * 
	 * @param id
	 * @throws LogicException
	 */
	void deleteNews(Integer id) throws LogicException;

	/**
	 * 查询最近的动态
	 * 
	 * @param limit
	 * @return
	 */
	List<News> queryLastNews(int limit, boolean queryLock);

	/**
	 * 查询动态统计
	 * 
	 * @return
	 */
	NewsStatistics queryNewsStatistics();

	/**
	 * 查询上下动态
	 * 
	 * @param id
	 *            当前动态ID
	 * @return
	 */
	Optional<NewsNav> getNewsNav(Integer id, boolean queryLock);

	/**
	 * 
	 * @param id
	 *            文章id
	 * @return 当前点击数
	 */
	void hit(Integer id);

	/**
	 * 查询上下动态
	 * <p>
	 * <b>仅供模板调用！！！</b>
	 * </p>
	 * 
	 * @param news
	 *            当前动态
	 * @return
	 */
	Optional<NewsNav> getNewsNav(News news, boolean queryLock);

	/**
	 * 分页查询动态归档
	 * 
	 * @param param
	 * @return
	 */
	PageResult<NewsArchive> queryNewsArchive(NewsArchivePageQueryParam param);

}
