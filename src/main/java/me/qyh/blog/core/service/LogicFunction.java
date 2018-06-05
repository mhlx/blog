package me.qyh.blog.core.service;

import me.qyh.blog.core.exception.LogicException;

@FunctionalInterface
public interface LogicFunction<T, R> {
	T apply(R t) throws LogicException;
}
