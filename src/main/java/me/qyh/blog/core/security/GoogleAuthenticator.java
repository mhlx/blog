package me.qyh.blog.core.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import me.qyh.blog.core.exception.SystemException;

/**
 * 谷歌OTP认证器
 * <p>
 * 在系统里配置这个之后，登录|修改密码等操作将会需要输入OTP
 * </p>
 * 
 * @author Administrator
 * @see LoginController
 */
public class GoogleAuthenticator {

	private static final String HMAC_HASH_FUNCTION = "HmacSHA1";
	private static final long MILL = TimeUnit.SECONDS.toMillis(30);

	private long lasttimeslot;

	private final String secret;
	private final int window;// 认证窗口

	public GoogleAuthenticator(String secret, int window) {
		super();
		this.secret = secret;
		try {
			Base32String.decode(secret);
		} catch (Exception e) {
			throw new SystemException("不是一个正确的Base32格式密钥:" + e.getMessage());
		}
		if (window < 1 || window > 17) {
			throw new SystemException("window值必须在1~17之间");
		}
		this.window = window;
	}

	public GoogleAuthenticator(String secret) {
		this(secret, 3);
	}

	/**
	 * 根据当前时间校验code
	 * 
	 * @param codeStr
	 *            验证码
	 * @return true 校验通过
	 */
	public boolean checkCode(String codeStr) {
		if (codeStr == null || codeStr.length() != 6) {
			return false;
		}
		int code;
		try {
			code = Integer.parseInt(codeStr);
		} catch (NumberFormatException e) {
			return false;
		}
		long timestamp = System.currentTimeMillis();
		byte[] decodedKey = decodeSecret(secret);
		final long timeWindow = getTimeWindowFromTime(timestamp);
		for (int i = -((window - 1) / 2); i <= window / 2; i++) {
			long slot = timeWindow + i;
			long hash = calculateCode(decodedKey, slot);
			if (hash == code) {
				if (lasttimeslot >= slot) {
					return false;
				}
				synchronized (this) {
					if (lasttimeslot < slot) {
						lasttimeslot = slot;
					}
				}
				return true;
			}
		}
		return false;
	}

	private byte[] decodeSecret(String secret) {
		return Base32String.decode(secret);
	}

	private int calculateCode(byte[] key, long tm) {
		// Allocating an array of bytes to represent the specified instant
		// of time.
		byte[] data = new byte[8];
		long value = tm;

		// Converting the instant of time from the long representation to a
		// big-endian array of bytes (RFC4226, 5.2. Description).
		for (int i = 8; i-- > 0; value >>>= 8) {
			data[i] = (byte) value;
		}

		// Building the secret key specification for the HmacSHA1 algorithm.
		SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);

		try {
			// Getting an HmacSHA1 algorithm implementation from the JCE.
			Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);

			// Initializing the MAC algorithm.
			mac.init(signKey);

			// Processing the instant of time and getting the encrypted data.
			byte[] hash = mac.doFinal(data);

			// Building the validation code performing dynamic truncation
			// (RFC4226, 5.3. Generating an HOTP value)
			int offset = hash[hash.length - 1] & 0xF;

			// We are using a long because Java hasn't got an unsigned integer
			// type
			// and we need 32 unsigned bits).
			long truncatedHash = 0;

			for (int i = 0; i < 4; ++i) {
				truncatedHash <<= 8;

				// Java bytes are signed but we need an unsigned integer:
				// cleaning off all but the LSB.
				truncatedHash |= (hash[offset + i] & 0xFF);
			}

			// Clean bits higher than the 32nd (inclusive) and calculate the
			// module with the maximum validation code value.
			truncatedHash &= 0x7FFFFFFF;
			truncatedHash %= 1000000;

			// Returning the validation code to the caller.
			return (int) truncatedHash;
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new SystemException(ex.getMessage(), ex);
		}
	}

	private long getTimeWindowFromTime(long time) {
		return time / MILL;
	}

}
