package me.qyh.blog.plugin.cachehits;

import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.event.TransactionalEventListener;

import me.qyh.blog.core.dao.ArticleDao;
import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.event.ArticleDelEvent;
import me.qyh.blog.core.service.impl.ArticleIndexer;
import me.qyh.blog.core.service.impl.Transactions;

class ArticleCacheableHitsStrategy extends CacheableHitsStrategy<Article> {

	@Autowired
	private ArticleDao articleDao;
	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	@Autowired
	private ArticleIndexer articleIndexer;

	private final int flushNum;

	public ArticleCacheableHitsStrategy(boolean cacheIp, int maxIps, int flushSec, int flushNum) {
		super(cacheIp, maxIps, flushSec);
		this.flushNum = flushNum;
	}

	@Override
	protected int getHits(Article e) {
		return articleDao.selectHits(e.getId());
	}

	@Override
	protected void doFlush(Map<Integer, Integer> hitsMap, boolean contextClose) {
		if (!contextClose) {
			Transactions.afterCommit(() -> {
				articleIndexer.updateHits(hitsMap);
			});
		}

		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
			ArticleDao articleDao = sqlSession.getMapper(ArticleDao.class);
			int num = 0;
			for (Map.Entry<Integer, Integer> it : hitsMap.entrySet()) {
				articleDao.updateHits(it.getKey(), it.getValue());
				num++;
				if (num % flushNum == 0) {
					sqlSession.commit();
				}
			}
			sqlSession.commit();
		}
	}

	@TransactionalEventListener
	public void handleArticleEvent(ArticleDelEvent evt) {
		if (!evt.isLogicDelete()) {
			evt.getArticles().stream().map(Article::getId).forEach(id -> {
				flushMap.remove(id);
				hitsMap.remove(id);
			});
		}
	}

}
