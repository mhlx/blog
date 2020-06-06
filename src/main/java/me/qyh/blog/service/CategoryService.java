package me.qyh.blog.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.entity.Category;
import me.qyh.blog.event.CategoryDeleteEvent;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CategoryMapper;

@Service
public class CategoryService {

	private final CategoryMapper categoryMapper;
	private final ApplicationEventPublisher publisher;

	public CategoryService(CategoryMapper categoryMapper, ApplicationEventPublisher publisher) {
		super();
		this.categoryMapper = categoryMapper;
		this.publisher = publisher;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int saveCategory(Category category) {
		if (categoryMapper.selectByName(category.getName()).isPresent()) {
			throw new LogicException("category.exists", "分类已经存在");
		}
		category.setCreateTime(LocalDateTime.now());
		categoryMapper.insert(category);
		return category.getId();
	}

	@Transactional(readOnly = true)
	public List<Category> getAllCategories() {
		return categoryMapper.selectAll();
	}

	@Transactional(readOnly = true)
	public Optional<Category> getCategory(String name) {
		return categoryMapper.selectByName(name);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteCategory(final int id) {
		Optional<Category> opCategory = categoryMapper.selectById(id);
		if (opCategory.isEmpty()) {
			return;
		}
		categoryMapper.deleteById(id);
		publisher.publishEvent(new CategoryDeleteEvent(this, opCategory.get()));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateCategory(final Category category) {
		Optional<Category> opCategory = categoryMapper.selectById(category.getId());
		if (opCategory.isEmpty()) {
			throw new ResourceNotFoundException("category.notExists", "分类不存在");
		}
		Category old = opCategory.get();
		if (old.getName().equals(category.getName())) {
			return;
		}
		old.setName(category.getName());
		old.setModifyTime(LocalDateTime.now());
		categoryMapper.update(old);
	}
}
