package me.qyh.blog.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.TagMapper;
import me.qyh.blog.service.SimpleCacheManager.SimpleCache;
import me.qyh.blog.service.event.TagDeleteEvent;

@Service
public class TagService {

	private final TagMapper tagMapper;
	private final ApplicationEventPublisher publisher;

	private SimpleCache<Tag> cache = SimpleCacheManager.get().getCache(TagService.class.getName());

	public TagService(TagMapper tagMapper, ApplicationEventPublisher publisher) {
		super();
		this.tagMapper = tagMapper;
		this.publisher = publisher;
		for (Tag tag : tagMapper.selectAll()) {
			cache.put(tag.getId(), tag);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int saveTag(Tag tag) {
		if (tagMapper.selectByName(tag.getName()).isPresent()) {
			throw new LogicException("tagService.save.exists", "标签已经存在");
		}
		tag.setCreateTime(LocalDateTime.now());
		tagMapper.insert(tag);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.put(tag.getId(), tag);
			}

		});
		return tag.getId();
	}

	public List<Tag> getAllTags() {
		List<Tag> categories = new ArrayList<>(cache.getAll());
		categories.sort(Comparator.comparing(Tag::getCreateTime).reversed());
		return Collections.unmodifiableList(categories);
	}

	public Optional<Tag> getTag(int id) {
		return Optional.ofNullable(cache.get(id));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteTag(final int id) {
		Optional<Tag> opTag = tagMapper.selectById(id);
		if (opTag.isEmpty()) {
			return;
		}
		tagMapper.deleteById(id);
		publisher.publishEvent(new TagDeleteEvent(this, opTag.get()));
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.remove(id);
			}

		});
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTag(final Tag tag) {
		Optional<Tag> opTag = tagMapper.selectById(tag.getId());
		if (opTag.isEmpty()) {
			throw new ResourceNotFoundException("tagService.delete.notExists", "标签不存在");
		}
		Tag old = opTag.get();
		if (old.getName().equals(tag.getName())) {
			return;
		}
		old.setName(tag.getName());
		old.setModifyTime(LocalDateTime.now());
		tagMapper.update(old);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.put(old.getId(), old);
			}

		});
	}
}
