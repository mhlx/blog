package me.qyh.blog;

public class Constants {

	private Constants() {
		super();
	}

	public static final Message SYSTEM_ERROR = new Message("system.error", "系统异常");
	public static final Message AUTH_ERROR = new Message("auth.fail", "用户认证失败");
	public static final String AUTH_TOTP_SESSION = "totp_authenticate_require";
	public static final String AUTH_SESSION_KEY = "authed";
	public static final String PASSWORD_SESSION_KEY = "password";
	public static final int MAX_PAGE_SIZE = 50;
	public static final String REDIRECT_URL_ATTRIBUTE = "redirectUrl";
}
