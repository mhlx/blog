package me.qyh.blog.core.security;

public final class BCrypts {

	private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

	public static boolean matches(String password, String encrypt) {
		return B_CRYPT_PASSWORD_ENCODER.matches(password, encrypt);
	}

	public static String encode(String password) {
		return B_CRYPT_PASSWORD_ENCODER.encode(password);
	}

}
