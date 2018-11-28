package me.qyh.blog.core.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.vo.NewsArchivePageQueryParam;
import me.qyh.blog.core.vo.NewsQueryParam;
import me.qyh.blog.core.vo.NewsStatistics;

public interface NewsDao {

	/**
	 * 查询符合条件的动态
	 * 
	 * @param param
	 * @return
	 */
	List<News> selectPage(NewsQueryParam param);

	/**
	 * 查询符合条件的动态数目
	 * 
	 * @param param
	 * @return
	 */
	int selectCount(NewsQueryParam param);

	/**
	 * 查询指定的动态
	 * 
	 * @param id
	 * @return
	 */
	News selectById(Integer id);

	/**
	 * 根据ID删除
	 * 
	 * @param id
	 */
	void deleteById(Integer id);

	/**
	 * 查询最近的动态
	 * 
	 * @param limit
	 * @param queryPrivate
	 * @return
	 */
	List<News> selectLast(@Param("limit") int limit, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryLock") boolean queryLock);

	void insert(News news);

	void update(News news);

	/**
	 * @since 6.1
	 * @return
	 */
	NewsStatistics selectStatistics(@Param("queryPrivate") boolean queryPrivate);

	/**
	 * 查询上一条动态
	 * 
	 * @param news
	 * @param queryPrivate
	 * @return
	 */
	News getPreviousNews(@Param("news") News news, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryLock") boolean queryLock);

	/**
	 * 查询下一条动态
	 * 
	 * @param news
	 * @param queryPrivate
	 * @return
	 */
	News getNextNews(@Param("news") News news, @Param("queryPrivate") boolean queryPrivate,
			@Param("queryLock") boolean queryLock);

	/**
	 * 获取动态的点击数
	 * 
	 * @param id
	 * @return
	 */
	int selectHits(Integer id);

	/**
	 * 更新动态的点击量
	 * 
	 * @param id
	 *            文章的id
	 * @param currentHits
	 *            <strong>当前的</strong>点击量
	 */
	void updateHits(@Param("id") Integer id, @Param("hits") int currentHits);

	/**
	 * 分页查询动态归档日期
	 * 
	 * @param param
	 * @return
	 * @since 7.0
	 */
	List<String> selectNewsDays(NewsArchivePageQueryParam param);

	/**
	 * 查询动态归档日期数
	 * 
	 * @param param
	 * @return
	 * @since 7.0
	 */
	int selectNewsDaysCount(NewsArchivePageQueryParam param);

	/**
	 * 检查是否存在动态被某锁保护
	 * 
	 * @since 7.0
	 * @param lockId
	 * @return
	 */
	boolean checkExistsByLockId(String lockId);

}
