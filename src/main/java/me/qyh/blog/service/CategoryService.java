package me.qyh.blog.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import me.qyh.blog.entity.Category;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CategoryMapper;
import me.qyh.blog.service.SimpleCacheManager.SimpleCache;
import me.qyh.blog.service.event.CategoryDeleteEvent;

@Service
public class CategoryService {

	private final CategoryMapper categoryMapper;
	private final ApplicationEventPublisher publisher;

	private SimpleCache<Category> cache = SimpleCacheManager.get().getCache(CategoryService.class.getName());

	public CategoryService(CategoryMapper categoryMapper, ApplicationEventPublisher publisher) {
		super();
		this.categoryMapper = categoryMapper;
		this.publisher = publisher;
		for (Category category : categoryMapper.selectAll()) {
			cache.put(category.getId(), category);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int saveCategory(Category category) {
		if (categoryMapper.selectByName(category.getName()).isPresent()) {
			throw new LogicException("categoryService.save.exists", "分类已经存在");
		}
		category.setCreateTime(LocalDateTime.now());
		categoryMapper.insert(category);
		TransactionSynchronizationManager.registerSynchronization(new OrderedTransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.put(category.getId(), category);
			}

			@Override
			public int getOrder() {
				return Ordered.HIGHEST_PRECEDENCE;
			}

		});
		return category.getId();
	}

	public List<Category> getAllCategories() {
		List<Category> categories = new ArrayList<>(cache.getAll());
		categories.sort(Comparator.comparing(Category::getCreateTime).reversed());
		return List.copyOf(categories);
	}

	public Optional<Category> getCategory(int id) {
		return Optional.ofNullable(cache.get(id));
	}

	public Optional<Category> getCategory(String name) {
		return cache.getAll().stream().filter(c -> c.getName().equals(name)).findAny();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteCategory(final int id) {
		Optional<Category> opCategory = categoryMapper.selectById(id);
		if (opCategory.isEmpty()) {
			return;
		}
		categoryMapper.deleteById(id);
		publisher.publishEvent(new CategoryDeleteEvent(this, opCategory.get()));
		TransactionSynchronizationManager.registerSynchronization(new OrderedTransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.remove(id);
			}

			@Override
			public int getOrder() {
				return Ordered.HIGHEST_PRECEDENCE;
			}

		});
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateCategory(final Category category) {
		Optional<Category> opCategory = categoryMapper.selectById(category.getId());
		if (opCategory.isEmpty()) {
			throw new ResourceNotFoundException("categoryService.delete.notExists", "分类不存在");
		}
		Category old = opCategory.get();
		if (old.getName().equals(category.getName())) {
			return;
		}
		old.setName(category.getName());
		old.setModifyTime(LocalDateTime.now());
		categoryMapper.update(old);
		TransactionSynchronizationManager.registerSynchronization(new OrderedTransactionSynchronization() {

			@Override
			public void afterCommit() {
				cache.put(old.getId(), old);
			}

			@Override
			public int getOrder() {
				return Ordered.HIGHEST_PRECEDENCE;
			}

		});
	}
}
