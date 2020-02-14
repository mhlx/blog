package me.qyh.blog.service;

import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.exception.AuthenticationException;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;

public interface CommentModuleHandler<T> {

	String getModuleName();

	/**
	 * 
	 * @param module
	 * @return the module target that contains required info that can access the
	 *         module target
	 * @throws ResourceNotFoundException if module target is not exists
	 * @throws LogicException            logic error that prevent query
	 * @throws AuthenticationException   authentication fail when try to access
	 *                                   module target
	 */
	T checkBeforeQuery(CommentModule module);

	/**
	 * 
	 * @param comment
	 * @param module
	 * @throws ResourceNotFoundException if module target is not exists
	 * @throws LogicException            logic error that prevent save
	 * @throws AuthenticationException   authentication fail when try to access
	 *                                   module target
	 */
	void checkBeforeSave(Comment comment, CommentModule module);
}