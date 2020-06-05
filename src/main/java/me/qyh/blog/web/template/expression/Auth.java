package me.qyh.blog.web.template.expression;

import me.qyh.blog.BlogContext;

class Auth {

	public boolean isAuthenticated() {
		return BlogContext.isAuthenticated();
	}
}
