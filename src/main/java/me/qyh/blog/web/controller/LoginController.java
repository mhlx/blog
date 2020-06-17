package me.qyh.blog.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import io.github.bucket4j.Bucket;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.Message;
import me.qyh.blog.exception.LoginFailException;
import me.qyh.blog.security.CaptchaValidator;
import me.qyh.blog.security.TokenUtil;
import me.qyh.blog.security.TwoFactorAuthenticator;
import me.qyh.blog.service.BlogConfigService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
public class LoginController {

    private final BlogConfigService configService;
    private final CaptchaValidator captchaValidator;
    private final Bucket bucket;
    private final BlogProperties blogProperties;
    private final TwoFactorAuthenticator twoFactorAuthenticator;

    private TwoFactorIdentifier twoFactorIdentifier;

    public LoginController(BlogConfigService configService, BlogProperties blogProperties,
                           CaptchaValidator captchaValidator, @Nullable TwoFactorAuthenticator twoFactorAuthenticator) {
        super();
        this.configService = configService;
        this.bucket = blogProperties.getLoginBucket();
        this.captchaValidator = captchaValidator;
        this.blogProperties = blogProperties;
        this.twoFactorAuthenticator = twoFactorAuthenticator;
    }

    @PostMapping("api/token")
    public String token(@RequestBody JsonNode node, HttpServletRequest request) {

        if (!bucket.tryConsume(1)) {
            String key = getNodeValueAsText(node, "captcha_key");
            String code = getNodeValueAsText(node, "captcha_value");
            captchaValidator.validate(key, code);
        }

        boolean twoFactorEnable = twoFactorAuthenticator != null;

        String name = getNodeValueAsText(node, "username");
        String password = getNodeValueAsText(node, "password");
        if (name != null && password != null) {
            if (!configService.authenticate(name, password)) {
                throw new LoginFailException(new Message("login.fail", "登录失败"));
            }
            if (twoFactorEnable) {
                twoFactorAuthenticator.afterUsernamePasswordAuthenticated();
                this.twoFactorIdentifier = new TwoFactorIdentifier();
                throw new LoginFailException(new TwoFactorMessage("login.fail.twoFactorRequired", "需要二次登录", this.twoFactorIdentifier.identifier));
            }
        } else if (name == null && password == null && twoFactorIdentifier != null && !twoFactorIdentifier.isExpired()) {
            String identifier = getNodeValueAsText(node, "identifier");
            String code;
            if (!twoFactorIdentifier.isValidIdentifier(identifier)
                    || ((code = getNodeValueAsText(node, "twoFactorCode")) == null) || !twoFactorAuthenticator.check(code)) {
                throw new LoginFailException(new Message("login.fail.twoFactorFail", "二次认证失败"));
            }
        } else {
            throw new LoginFailException(new Message("login.fail", "登录失败"));
        }

        if (!blogProperties.isCors()) {
            request.getSession().setAttribute(Constants.AUTHENTICATED_SESSION_KEY, Boolean.TRUE);
        }

        return TokenUtil.create();
    }

    private String getNodeValueAsText(JsonNode node, String fieldName) {
        JsonNode _node = node.get(fieldName);
        if (_node == null || _node == NullNode.getInstance() || !_node.isValueNode()) {
            return null;
        }
        return _node.asText();
    }

    private static final class TwoFactorIdentifier {
        private final String identifier;
        private final long created;

        private static final long EXPIRED_TIME = 5 * 60 * 1000L;

        public TwoFactorIdentifier() {
            this.identifier = UUID.randomUUID().toString().replaceAll("-", "");
            this.created = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - created > EXPIRED_TIME;
        }

        public boolean isValidIdentifier(String identifier) {
            return this.identifier.equals(identifier);
        }
    }

    public static final class TwoFactorMessage extends Message {

        private final String identifier;

        private TwoFactorMessage(String code, String defaultMessage, String identifier, Object... args) {
            super(code, defaultMessage, args);
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return this.identifier;
        }
    }
}
