package me.qyh.blog.core.service;

import me.qyh.blog.core.exception.LogicException;

@FunctionalInterface
public interface LogicConsumer<R> {
	void accept(R t) throws LogicException;
}