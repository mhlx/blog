package me.qyh.blog.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

public class EmptyView implements View {

	public static final EmptyView VIEW = new EmptyView();

	private EmptyView() {
		super();
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

	}

	@Override
	public String getContentType() {
		return null;
	}

}
