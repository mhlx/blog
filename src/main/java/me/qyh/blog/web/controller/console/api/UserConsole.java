package me.qyh.blog.web.controller.console.api;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.core.service.UserService;
import me.qyh.blog.core.validator.UserValidator;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class UserConsole extends BaseMgrController {

	@Autowired
	private UserValidator userValidator;
	@Autowired
	private UserService userService;
	@Autowired(required = false)
	private GoogleAuthenticator ga;

	@InitBinder(value = "user")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(userValidator);
	}

	@GetMapping("user")
	public User get() {
		User user = userService.getUser();
		user.setPassword(null);
		return user;
	}

	@PutMapping("user")
	public ResponseEntity<Void> update(@RequestParam(value = "oldPassword") String oldPassword,
			@RequestParam(value = "code", required = false) String codeStr, @Validated @RequestBody User user,
			HttpSession session) throws LogicException {
		if (ga != null && !ga.checkCode(codeStr)) {
			throw new LogicException("otp.verifyFail", "动态口令校验失败");
		}
		userService.update(user, oldPassword);
		session.setAttribute(Constants.USER_SESSION_KEY, user);
		return ResponseEntity.noContent().build();
	}

}
