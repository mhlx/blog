package me.qyh.blog.core.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.dao.ArticleTagDao;
import me.qyh.blog.core.dao.TagDao;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.TagService;
import me.qyh.blog.core.vo.PageResult;
import me.qyh.blog.core.vo.TagDetailStatistics;
import me.qyh.blog.core.vo.TagQueryParam;
import me.qyh.blog.core.vo.TagStatistics;

@Service
public class TagServiceImpl implements TagService {

	@Autowired
	private TagDao tagDao;
	@Autowired
	private ArticleTagDao articleTagDao;
	@Autowired
	private ConfigServer configSerivce;
	@Autowired
	private ArticleIndexer articleIndexer;

	@Override
	@Transactional(readOnly = true)
	public PageResult<Tag> queryTag(TagQueryParam param) {
		param.setPageSize(configSerivce.getGlobalConfig().getTagPageSize());
		int count = tagDao.selectCount(param);
		List<Tag> datas = tagDao.selectPage(param);
		return new PageResult<>(param, count, datas);
	}

	@Override
	@Sync
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public Tag updateTag(Tag tag, boolean merge) throws LogicException {
		Optional<Tag> op = tagDao.selectById(tag.getId());
		if (op.isEmpty()) {
			throw new LogicException("tag.notExists", "标签不存在");
		}
		Tag db = op.get();
		if (db.getName().equals(tag.getName())) {
			return tag;
		}
		Optional<Tag> newOp = tagDao.selectByName(tag.getName());
		if (newOp.isPresent()) {
			if (!merge) {
				throw new LogicException("tag.exists", "标签已经存在");
			}
			articleTagDao.merge(db, newOp.get());
			tagDao.deleteById(db.getId());
		} else {
			tagDao.update(tag);
		}

		Transactions.afterCommit(() -> {
			articleIndexer.removeTags(db.getName());
			articleIndexer.addTags(tag.getName());
			articleIndexer.rebuildIndex();
		});

		return tag;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void deleteTag(Integer id) throws LogicException {
		Optional<Tag> op = tagDao.selectById(id);
		if (op.isPresent()) {
			throw new LogicException("tag.notExists", "标签不存在");
		}
		Tag db = op.get();
		articleTagDao.deleteByTag(db);
		tagDao.deleteById(id);

		Transactions.afterCommit(() -> {
			articleIndexer.removeTags(db.getName());
			articleIndexer.rebuildIndex();
		});
	}

	@Override
	@Transactional(readOnly = true)
	public TagDetailStatistics queryTagDetailStatistics(Space space) {
		TagDetailStatistics tagDetailStatistics = new TagDetailStatistics();
		tagDetailStatistics.setArticleTagCount(articleTagDao.selectAllTagsCount(space));
		if (space == null) {
			tagDetailStatistics.setTotal(tagDao.selectCount(new TagQueryParam()));
		}
		return tagDetailStatistics;
	}

	@Override
	@Transactional(readOnly = true)
	public TagStatistics queryTagStatistics() {
		TagStatistics tagStatistics = new TagStatistics();
		boolean queryPrivate = Environment.hasAuthencated();
		tagStatistics.setArticleTagCount(articleTagDao.selectTagsCount(Environment.getSpace(), queryPrivate));
		return tagStatistics;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Tag> getTag(Integer id) {
		return tagDao.selectById(id);
	}

}
