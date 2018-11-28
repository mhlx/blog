package me.qyh.blog.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import me.qyh.blog.web.Webs;

public class JsonView implements View {

	private final Object result;

	public JsonView(Object result) {
		super();
		this.result = result;
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON_UTF8_VALUE;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Webs.writeInfo(response, result);
	}

}
