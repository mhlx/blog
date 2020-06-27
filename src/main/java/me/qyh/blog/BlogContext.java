package me.qyh.blog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author wwwqyhme
 */
public class BlogContext {

    private static final ThreadLocal<Boolean> Authenticated_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> IP_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> PASSWORD_LOCAL = ThreadLocal.withInitial(HashMap::new);

    public static void setAuthenticated(boolean authenticated) {
        Authenticated_LOCAL.set(authenticated);
    }

    public static boolean isAuthenticated() {
        return Optional.ofNullable(Authenticated_LOCAL.get()).orElse(false);
    }

    public static void clear() {
        Authenticated_LOCAL.remove();
        IP_LOCAL.remove();
        PASSWORD_LOCAL.remove();
    }

    public static void setIP(String ip) {
        IP_LOCAL.set(ip);
    }

    public static void setPasswordMap(Map<String, String> pwdMap) {
        if (pwdMap != null) {
            PASSWORD_LOCAL.set(pwdMap);
        }
    }

    /**
     * 获取当前请求的IP
     */
    public static Optional<String> getIP() {
        return Optional.ofNullable(IP_LOCAL.get());
    }

    public static Map<String, String> getPasswordMap() {
        return Map.copyOf(PASSWORD_LOCAL.get());
    }
}
