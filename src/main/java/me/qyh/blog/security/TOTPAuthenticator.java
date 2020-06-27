package me.qyh.blog.security;

import me.qyh.blog.BlogProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Google OTP 认证
 *
 * @author Administrator
 */
@Component
@ConditionalOnProperty(prefix = "blog.core", name = "totp-enable", havingValue = "true")
public class TOTPAuthenticator implements TwoFactorAuthenticator {

    private static final String HMAC_HASH_FUNCTION = "HmacSHA1";
    private static final long MILL = TimeUnit.SECONDS.toMillis(30);

    private long lasttimeslot;

    private final String secret;
    private final int window;// 认证窗口

    private static final Base32String b32String = new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");

    public TOTPAuthenticator(BlogProperties properties) {
        super();
        this.secret = properties.getTotpSecret();
        this.window = properties.getTotpWindow();
    }

    /**
     * 根据当前时间校验code
     *
     * @param codeStr 验证码
     * @return true 校验通过
     */
    @Override
    public boolean check(String codeStr) {
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

    public static void validateToken(String token) {
        b32String.decode(token);
    }

    private byte[] decodeSecret(String secret) {
        return b32String.decode(secret);
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
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private long getTimeWindowFromTime(long time) {
        return time / MILL;
    }

    private static final class Base32String {
        private final int MASK;
        private final int SHIFT;
        private final HashMap<Character, Integer> CHAR_MAP;

        static final String SEPARATOR = "-";

        public Base32String(String alphabet) {
            // 32 alpha-numeric characters.
            char[] DIGITS = alphabet.toCharArray();
            MASK = DIGITS.length - 1;
            SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
            CHAR_MAP = new HashMap<>();
            for (int i = 0; i < DIGITS.length; i++) {
                CHAR_MAP.put(DIGITS[i], i);
            }
        }

        public byte[] decode(String encoded) {
            return this.decodeInternal(encoded);
        }

        protected byte[] decodeInternal(String encoded) {
            // Remove whitespace and separators
            encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");

            // Remove padding. Note: the padding is used as hint to determine how
            // many
            // bits to decode from the last incomplete chunk (which is commented out
            // below, so this may have been wrong to start with).
            encoded = encoded.replaceFirst("[=]*$", "");

            // Canonicalize to all upper case
            encoded = encoded.toUpperCase(Locale.US);
            if (encoded.length() == 0) {
                return new byte[0];
            }
            int encodedLength = encoded.length();
            int outLength = encodedLength * SHIFT / 8;
            byte[] result = new byte[outLength];
            int buffer = 0;
            int next = 0;
            int bitsLeft = 0;
            for (char c : encoded.toCharArray()) {
                if (!CHAR_MAP.containsKey(c)) {
                    throw new RuntimeException("Illegal character: " + c);
                }
                buffer <<= SHIFT;
                buffer |= CHAR_MAP.get(c) & MASK;
                bitsLeft += SHIFT;
                if (bitsLeft >= 8) {
                    result[next++] = (byte) (buffer >> (bitsLeft - 8));
                    bitsLeft -= 8;
                }
            }
            // We'll ignore leftover bits for now.
            //
            // if (next != outLength || bitsLeft >= SHIFT) {
            // throw new SystemException("Bits left: " + bitsLeft);
            // }
            return result;
        }

        @Override
        // enforce that this class is a singleton
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }
}
