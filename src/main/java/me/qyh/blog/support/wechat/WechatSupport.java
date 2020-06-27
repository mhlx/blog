package me.qyh.blog.support.wechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.qyh.blog.utils.StreamUtils;
import org.jsoup.UncheckedIOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.UUID;

@ConditionalOnProperty(prefix = "blog.support.wechat", name = {"appid", "appsecret"})
@Component
public class WechatSupport {

    private static final long TOKEN_EXPIRE_SEC = 7100 * 1000L;// 实际7200s
    private static final long TICKET_EXPIRE_SEC = 7100 * 1000L;// 实际7200s
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";

    private Token token;
    private Ticket ticket;

    private final WechatProperties wechatProperties;
    private final ObjectMapper objectMapper;

    public WechatSupport(WechatProperties wechatProperties, ObjectMapper objectMapper) {
        this.wechatProperties = wechatProperties;
        this.objectMapper = objectMapper;
    }

    private void refreshToken() {
        if (token != null && !token.overtime()) {
            return;
        }
        synchronized (this) {
            if (token != null && !token.overtime()) {
                return;
            }
            String tokenUrl = String.format(TOKEN_URL, wechatProperties.getAppid(), wechatProperties.getAppsecret());
            try (InputStream is = new UrlResource(new URL(tokenUrl)).getInputStream()) {
                String content = StreamUtils.toString(is);
                JsonNode node = objectMapper.readTree(content);
                JsonNode tokenNode = node.get("access_token");
                if (tokenNode == null) {
                    throw new RuntimeException("error when get wechat token:" + content);
                }
                this.token = new Token(System.currentTimeMillis(), tokenNode.asText());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
            String ticketUrl = String.format(TICKET_URL, token.getToken());
            try (InputStream is = new UrlResource(new URL(ticketUrl)).getInputStream()) {
                String content = StreamUtils.toString(is);
                JsonNode node = objectMapper.readTree(content);
                JsonNode errorNode = node.get("errcode");
                if (errorNode.asInt() != 0) {
                    throw new RuntimeException("error when get wechat ticket:" + content);
                }
                this.ticket = new Ticket(System.currentTimeMillis(), node.get("ticket").asText());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    protected String createNoncestr() {
        return UUID.randomUUID().toString();
    }

    public Signature createSignature(String url) {
        refreshTicket();
        String noncestr = createNoncestr();
        long timestamp = System.currentTimeMillis() / 1000L;

        String sb = "jsapi_ticket=" + ticket.getTicket() + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url="
                + url;
        String signature = sha1(sb);

        return new Signature(noncestr, wechatProperties.getAppid(), timestamp, signature);
    }

    private String sha1(String str) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes(StandardCharsets.UTF_8));
            return byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
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

    public static final class Signature {
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

    private static final class Token {
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

    private static final class Ticket {
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
}
