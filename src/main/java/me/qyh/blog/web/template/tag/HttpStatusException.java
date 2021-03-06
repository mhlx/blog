package me.qyh.blog.web.template.tag;

import me.qyh.blog.Message;
import org.springframework.http.HttpStatus;

public class HttpStatusException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;
    private final Message error;

    HttpStatusException(HttpStatus status, Message error) {
        super(null, null, false, false);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Message getError() {
        return error;
    }

}
