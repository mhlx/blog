package me.qyh.blog.security;

import org.springframework.core.Ordered;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoginChain extends Ordered {

	void valid(JsonNode node);

}
