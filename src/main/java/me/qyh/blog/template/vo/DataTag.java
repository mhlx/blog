package me.qyh.blog.template.vo;

import java.util.Map;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

public class DataTag {

	private String name;
	private final Map<String, Object> attrs;

	public String getName() {
		return name;
	}

	public DataTag(String name, Map<String, Object> attrs) {
		this.name = name;
		if (attrs == null) {
			this.attrs = Map.of();
		} else {
			this.attrs = attrs;
		}
	}

	public Map<String, Object> getAttrs() {
		return attrs;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void put(String key, String v) {
		attrs.put(key, v);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attrs, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			DataTag other = (DataTag) obj;
			return Objects.equals(this.attrs, other.attrs) && Objects.equals(this.name, other.name);
		}
		return false;
	}

	public boolean hasKey(String key) {
		return attrs.containsKey(key);
	}

	@Override
	public String toString() {
		return "DataTag [name=" + name + ", attrs=" + attrs + "]";
	}

}
