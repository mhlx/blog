package me.qyh.blog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.springframework.context.MessageSourceResolvable;

@JsonAppend(
        props = {
                @JsonAppend.Prop(value = VirtualMessagePropertyWriter.class, type = String.class, name = "message")
        }
)
public class Message implements MessageSourceResolvable {
    private final String code;
    private final Object[] args;
    private final String defaultMessage;

    public Message(String code, String defaultMessage, Object... args) {
        super();
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.args = args;
    }

    @Override
    @JsonIgnore
    public String[] getCodes() {
        return new String[]{code};
    }

    @Override
    @JsonIgnore
    public Object[] getArguments() {
        return this.args;
    }

    @Override
    @JsonIgnore
    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    public String getCode() {
        return code;
    }
}
