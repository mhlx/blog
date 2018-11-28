package me.qyh.blog.core.service;

import me.qyh.blog.core.entity.BaseEntity;

/**
 * 
 * @author wwwqyhme
 *
 * @param <T>
 */
public interface HitsStrategy<T extends BaseEntity> {
	void hit(T t);
}
