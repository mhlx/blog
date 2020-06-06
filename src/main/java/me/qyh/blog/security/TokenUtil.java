package me.qyh.blog.security;

import java.util.UUID;

public class TokenUtil {

	private static final long LIVE = 30 * 60 * 1000L;
	private static Token token;

	private TokenUtil() {
		super();
	}

	public static String create() {
		token = new Token();
		return token.token;
	}

	public static boolean valid(String str, boolean increaseTime) {
		return token != null && token.valid(str, increaseTime);
	}

	public static boolean valid(String str) {
		return valid(str, true);
	}

	public static void remove() {
		token = null;
	}

	private static final class Token {
		private final String token;
		private long mill;

		public Token() {
			super();
			this.token = UUID.randomUUID().toString().replace("-", "");
			this.mill = System.currentTimeMillis();
		}

		public boolean valid(String token, boolean increaseTime) {
			long now = System.currentTimeMillis();
			if (now - mill > LIVE) {
				return false;
			}
			if (token != null && token.equals(this.token)) {
				if (increaseTime) {
					this.mill = now;
				}
				return true;
			}
			return false;
		}
	}

}
