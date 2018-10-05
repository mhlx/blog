/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.plugin.wechat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.Jsons.ExpressionExecutor;
import me.qyh.blog.core.util.StringUtils;

public class WechatSupport {

	private final String appid;
	private final String appsecret;

	private static final long TOKEN_EXPIRE_SEC = 7100 * 1000L;// 实际7200s
	private static final long TICKET_EXPIRE_SEC = 7100 * 1000L;// 实际7200s

	// TODO
	private final String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	private final String ticketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";

	private Token token;
	private Ticket ticket;

	WechatSupport(String appid, String appsecret) {
		this.appid = appid;
		this.appsecret = appsecret;
	}

	private void refreshToken() {
		if (token != null && !token.overtime()) {
			return;
		}
		synchronized (this) {
			if (token != null && !token.overtime()) {
				return;
			}
			String tokenUrl = String.format(this.tokenUrl, appid, appsecret);
			ExpressionExecutor executor = read(tokenUrl);
			String token = executor.execute("access_token")
					.orElseThrow(() -> new SystemException("获取access_token失败：" + executor.toString()));
			this.token = new Token(System.currentTimeMillis(), token);
		}
	}

	private void refreshTicket() {
		refreshToken();
		if (ticket != null && !ticket.overtime()) {
			return;
		}
		synchronized (this) {
			if (ticket != null && !ticket.overtime()) {
				return;
			}
			String ticketUrl = String.format(this.ticketUrl, token.getToken());
			ExpressionExecutor executor = read(ticketUrl);
			String ticket = executor.execute("ticket").orElseThrow(
					() -> new SystemException("获取ticket失败，失败信息：" + executor.toString() + "，token :" + token));
			this.ticket = new Ticket(System.currentTimeMillis(), ticket);
		}
	}

	protected String createNoncestr() {
		return StringUtils.uuid();
	}

	public Signature createSignature(String url) {
		refreshTicket();
		String noncestr = createNoncestr();
		long timestamp = System.currentTimeMillis() / 1000L;

		String sb = "jsapi_ticket=" + ticket.getTicket() + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url="
				+ url;
		String signature = sha1(sb);

		return new Signature(noncestr, appid, timestamp, signature);
	}

	private String sha1(String str) {
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(str.getBytes(StandardCharsets.UTF_8));
			return byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	private String byteToHex(final byte[] hash) {
		try (Formatter formatter = new Formatter()) {
			for (byte b : hash) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		}
	}

	public final class Signature {
		private final String noncestr;
		private final String appid;
		private final long timestamp;
		private final String signature;

		private Signature(String noncestr, String appid, long timestamp, String signature) {
			super();
			this.noncestr = noncestr;
			this.appid = appid;
			this.timestamp = timestamp;
			this.signature = signature;
		}

		public String getNoncestr() {
			return noncestr;
		}

		public String getAppid() {
			return appid;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public String getSignature() {
			return signature;
		}

		@Override
		public String toString() {
			return "Signature [noncestr=" + noncestr + ", appid=" + appid + ", timestamp=" + timestamp + ", signature="
					+ signature + "]";
		}
	}

	private final class Token {
		private final long time;
		private final String token;

		public Token(long time, String token) {
			super();
			this.time = time;
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		public boolean overtime() {
			return System.currentTimeMillis() - time > TOKEN_EXPIRE_SEC;
		}

	}

	private final class Ticket {
		private final long time;
		private final String ticket;

		public Ticket(long time, String ticket) {
			super();
			this.time = time;
			this.ticket = ticket;
		}

		public String getTicket() {
			return ticket;
		}

		public boolean overtime() {
			return System.currentTimeMillis() - time > TICKET_EXPIRE_SEC;
		}

	}

	private ExpressionExecutor read(String url) {
		ExpressionExecutor executor = Jsons.read(url);

		Throwable ex = executor.getEx();
		if (ex != null) {
			throw new SystemException(ex.getMessage(), ex);
		}
		return executor;
	}
}
