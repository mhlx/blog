package me.qyh.blog.web.controller.front;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.WebUtils;

@Controller
public class ErrorController {

	@GetMapping("error/ui")
	public String handlerUI(HttpServletRequest request) {
		String requestUri = (String) request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE);
		if (requestUri == null) {
			return "redirect:/";
		}
		return "error/ui";
	}

}
