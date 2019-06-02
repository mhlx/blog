package me.qyh.blog.plugin.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Jsons;

public class GitlabOauthProvider implements OauthProvider {

	private static final String AUTHORIZE_URL = "https://gitlab.com/oauth/authorize";
	private static final String TOKEN_URL = "https://gitlab.com/oauth/token";
	private static final String API_URL = "https://gitlab.com/api/v4/user";

	private final String clientId;
	private final String clientSecret;
	private final String account;

	public GitlabOauthProvider(String clientId, String clientSecret, String account) {
		super();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.account = account;
	}

	@Override
	public String getAuthorizeUrl(String redirectUrl, String state) {
		return AUTHORIZE_URL + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code&state="
				+ state;
	}

	@Override
	public void validate(String code, String state, String redirectUrl) throws LogicException {
		String token = getToken(code, state, redirectUrl);
		String account = getAccount(token);

		if (!account.equals(this.account)) {
			throw new LogicException("oauth.gitlab.unmatchAccount", "不匹配的Github账号");
		}
	}

	@Override
	public String getName() {
		return "gitlab";
	}

	private String getToken(String code, String state, String redirectUrl) throws LogicException {
		StringBuilder sb = new StringBuilder();
		try {
			URL obj = new URL(TOKEN_URL + "?client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code
					+ "&state=" + state + "&grant_type=authorization_code&redirect_uri=" + redirectUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

			try (InputStreamReader is = new InputStreamReader(con.getInputStream());
					BufferedReader in = new BufferedReader(is)) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
			}
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}

		return Jsons.readJson(sb.toString()).execute("access_token")
				.orElseThrow(() -> new LogicException("oauth.github.getTokenFail", "获取凭证失败"));
	}

	private String getAccount(String token) throws LogicException {
		StringBuilder sb = new StringBuilder();
		try {
			URL obj = new URL(API_URL + "?access_token=" + token);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");

			int statusCode = con.getResponseCode();

			if (statusCode == 401) {
				throw new LogicException("oauth.gitlab.token.invalid", "无效的凭证");
			}

			try (InputStreamReader is = new InputStreamReader(con.getInputStream());
					BufferedReader in = new BufferedReader(is)) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
			}
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}

		return Jsons.readJson(sb.toString()).execute("username")
				.orElseThrow(() -> new LogicException("oauth.gitlab.getAccountFail", "获取Github账户失败"));
	}

}
