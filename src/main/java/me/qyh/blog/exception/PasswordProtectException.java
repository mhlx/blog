package me.qyh.blog.exception;

public class PasswordProtectException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String id;
    private final boolean missPassword;

    public PasswordProtectException(String id, boolean missPassword) {
        super();
        this.id = id;
        this.missPassword = missPassword;
    }

    public boolean isMissPassword() {
        return missPassword;
    }

    public String getId() {
        return id;
    }

}
