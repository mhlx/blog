package me.qyh.blog.plugin.cachehits;

import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.event.TransactionalEventListener;

import me.qyh.blog.core.dao.NewsDao;
import me.qyh.blog.core.entity.News;
import me.qyh.blog.core.event.NewsDelEvent;

class NewsCacheableHitsStrategy extends CacheableHitsStrategy<News> {

	@Autowired
	private NewsDao newsDao;
	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	private final int flushNum;

	public NewsCacheableHitsStrategy(boolean cacheIp, int maxIps, int flushSec, int flushNum) {
		super(cacheIp, maxIps, flushSec);
		this.flushNum = flushNum;
	}

	@Override
	protected int getHits(News news) {
		return newsDao.selectHits(news.getId());
	}

	@Override
	protected void doFlush(Map<Integer, Integer> hitsMap, boolean contextClose) {
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
			NewsDao newsDao = sqlSession.getMapper(NewsDao.class);
			int num = 0;
			for (Map.Entry<Integer, Integer> it : hitsMap.entrySet()) {
				newsDao.updateHits(it.getKey(), it.getValue());
				num++;
				if (num % flushNum == 0) {
					sqlSession.commit();
				}
			}
			sqlSession.commit();
		}
	}

	@TransactionalEventListener
	public void handleNewsEvent(NewsDelEvent evt) {
		evt.getNewsList().stream().map(News::getId).forEach(id -> {
			flushMap.remove(id);
			hitsMap.remove(id);
		});
	}
}
