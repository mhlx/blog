package me.qyh.blog.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;

import me.qyh.blog.Message;
import me.qyh.blog.exception.LoginFailException;

/**
 * 
 * @author Administrator
 */
@Controller
@RequestMapping
@ConditionalOnProperty(prefix = "blog.core", name = "totp-enable", havingValue = "true")
public class TOTPChain implements LoginChain {

	private final TOTPAuthenticator authenticator;

	public TOTPChain(TOTPAuthenticator authenticator) {
		super();
		this.authenticator = authenticator;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void valid(JsonNode node) {
		JsonNode totpNode = node.get("totp");
		if (totpNode == null || !authenticator.check(totpNode.asText())) {
			throw new LoginFailException(new Message("login.fail.totpInvalid", "二次认证码错误"));
		}
	}
}
