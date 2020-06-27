package me.qyh.blog;

public class FieldMessageSourceResolvable extends Message {

    private final String field;

    public FieldMessageSourceResolvable(String code, String defaultMessage, String field, Object... args) {
        super(code, defaultMessage, args);
        this.field = field;
    }

    public String getField() {
        return this.field;
    }
}
