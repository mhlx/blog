package me.qyh.blog.exception;

import me.qyh.blog.Message;

/**
 * 逻辑异常，仅用于错误提示，不记录日志
 *
 * @author wwwqyhme
 */
public class LogicException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Message error;

    public LogicException(String code, String defaultMessage, Object... args) {
        this(new Message(code, defaultMessage, args));
    }

    public LogicException(Message error) {
        super(null, null, false, false);
        this.error = error;
    }

    public Message getError() {
        return error;
    }
}
