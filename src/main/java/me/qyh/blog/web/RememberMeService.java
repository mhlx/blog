package me.qyh.blog.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.entity.User;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.security.Hex;
import me.qyh.blog.core.service.UserService;

public class RememberMeService {

	private static final long TWO_WEEKS = 1209600 * 1000L;
	private static final String DELIMITER = ":";
	private static final String COOKIE_NAME = "remember-me";

	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private UserService userService;

	private final String key;

	public RememberMeService(String key) {
		super();
		this.key = key;
	}

	public void rememberMe(User user, HttpServletRequest request, HttpServletResponse response) {
		long tokenExpiryTime = System.currentTimeMillis() + TWO_WEEKS;
		String signature = makeTokenSignature(tokenExpiryTime, user);
		String cookieValue = encodeCookie(new String[] { user.getName(), Long.toString(tokenExpiryTime), signature });

		urlHelper.getCookieHelper().addCookie(COOKIE_NAME, cookieValue, (int) TWO_WEEKS / 1000, request, response);
	}

	public Optional<User> autoLogin(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
		if (cookie == null) {
			return Optional.empty();
		}
		String signature = cookie.getValue();
		if (signature == null) {
			return Optional.empty();
		}
		if (signature.isEmpty()) {
			deleteRememberMe(request, response);
			return Optional.empty();
		}
		try {
			String[] cookieTokens = decodeCookie(cookie.getValue());
			return Optional.of(processLogin(cookieTokens));
		} catch (InvalidCookieException e) {
			deleteRememberMe(request, response);
			return Optional.empty();
		}
	}

	public void deleteRememberMe(HttpServletRequest request, HttpServletResponse response) {
		urlHelper.getCookieHelper().deleteCookie(COOKIE_NAME, request, response);
	}

	private User processLogin(String[] cookieTokens) throws InvalidCookieException {
		if (cookieTokens.length != 3) {
			throw new InvalidCookieException(
					"Cookie token did not contain 3" + " tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
		}

		long tokenExpiryTime;

		try {
			tokenExpiryTime = Long.parseLong(cookieTokens[1]);
		} catch (NumberFormatException nfe) {
			throw new InvalidCookieException(
					"Cookie token[1] did not contain a valid number (contained '" + cookieTokens[1] + "')");
		}

		if (isTokenExpired(tokenExpiryTime)) {
			throw new InvalidCookieException("Cookie token[1] has expired (expired on '" + new Date(tokenExpiryTime)
					+ "'; current time is '" + new Date() + "')");
		}

		User user = userService.getUser();

		String expectedTokenSignature = makeTokenSignature(tokenExpiryTime, user);

		if (!equals(expectedTokenSignature, cookieTokens[2])) {
			throw new InvalidCookieException("Cookie token[2] contained signature '" + cookieTokens[2]
					+ "' but expected '" + expectedTokenSignature + "'");
		}

		return user;
	}

	protected boolean isTokenExpired(long tokenExpiryTime) {
		return tokenExpiryTime < System.currentTimeMillis();
	}

	protected String makeTokenSignature(long tokenExpiryTime, User user) {
		String data = user.getName() + ":" + tokenExpiryTime + ":" + user.getPassword() + ":" + key;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		return new String(Hex.encode(digest.digest(data.getBytes())));
	}

	protected String encodeCookie(String[] cookieTokens) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cookieTokens.length; i++) {
			try {
				sb.append(URLEncoder.encode(cookieTokens[i], StandardCharsets.UTF_8.toString()));
			} catch (UnsupportedEncodingException e) {
				throw new SystemException(e.getMessage(), e);
			}

			if (i < cookieTokens.length - 1) {
				sb.append(DELIMITER);
			}
		}

		String value = sb.toString();

		sb = new StringBuilder(new String(Base64.getEncoder().encode(value.getBytes())));

		while (sb.charAt(sb.length() - 1) == '=') {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	protected String[] decodeCookie(String cookieValue) throws InvalidCookieException {
		for (int j = 0; j < cookieValue.length() % 4; j++) {
			cookieValue = cookieValue + "=";
		}

		try {
			Base64.getDecoder().decode(cookieValue.getBytes());
		} catch (IllegalArgumentException e) {
			throw new InvalidCookieException("Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
		}

		String cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));

		String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, DELIMITER);

		for (int i = 0; i < tokens.length; i++) {
			try {
				tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				throw new SystemException(e.getMessage(), e);
			}
		}

		return tokens;
	}

	private static boolean equals(String expected, String actual) {
		byte[] expectedBytes = bytesUtf8(expected);
		byte[] actualBytes = bytesUtf8(actual);
		if (expectedBytes.length != actualBytes.length) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < expectedBytes.length; i++) {
			result |= expectedBytes[i] ^ actualBytes[i];
		}
		return result == 0;
	}

	private static byte[] bytesUtf8(String s) {
		if (s == null) {
			return null;
		}
		return encode(s);
	}

	private static byte[] encode(CharSequence string) {
		try {
			ByteBuffer bytes = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(string));
			byte[] bytesCopy = new byte[bytes.limit()];
			System.arraycopy(bytes.array(), 0, bytesCopy, 0, bytes.limit());

			return bytesCopy;
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Encoding failed", e);
		}
	}

	private final class InvalidCookieException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		InvalidCookieException(String message) {
			super(message);
		}

	}

}
