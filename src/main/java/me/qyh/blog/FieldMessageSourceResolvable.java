package me.qyh.blog;

import org.springframework.context.MessageSourceResolvable;

public class FieldMessageSourceResolvable implements MessageSourceResolvable {

	private final String field;
	private final MessageSourceResolvable message;

	public FieldMessageSourceResolvable(String field, MessageSourceResolvable message) {
		super();
		this.field = field;
		this.message = message;
	}

	public String getField() {
		return field;
	}

	@Override
	public String[] getCodes() {
		return this.message.getCodes();
	}

	@Override
	public Object[] getArguments() {
		return this.message.getArguments();
	}

	@Override
	public String getDefaultMessage() {
		return this.message.getDefaultMessage();
	}

	@Override
	public String toString() {
		return "FieldMessageSourceResolvable [field=" + field + ", message=" + message + "]";
	}

}
