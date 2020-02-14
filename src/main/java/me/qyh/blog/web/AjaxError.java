package me.qyh.blog.web;

import java.util.List;

import org.springframework.context.MessageSourceResolvable;

public class AjaxError {
	private final List<MessageSourceResolvable> errors;

	public AjaxError(List<MessageSourceResolvable> errors) {
		super();
		this.errors = errors;
	}

	public AjaxError(MessageSourceResolvable error) {
		super();
		this.errors = List.of(error);
	}

	public List<MessageSourceResolvable> getErrors() {
		return errors;
	}

}