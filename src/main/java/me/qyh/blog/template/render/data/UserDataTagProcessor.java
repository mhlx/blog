package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.UserService;

public class UserDataTagProcessor extends DataTagProcessor<User> {

	@Autowired
	private UserService userService;

	public UserDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected User query(Attributes attributes) throws LogicException {
		User user = userService.getUser();
		user.setPassword(null);
		return user;
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}

}
