package me.qyh.blog.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import me.qyh.blog.utils.WebUtils;

@Controller
@RequestMapping("/error")
public class BlogErrorController implements ErrorController {

	private final ErrorAttributes errorAttributes;

	public BlogErrorController(ErrorAttributes errorAttributes) {
		super();
		this.errorAttributes = errorAttributes;
	}

	@Deprecated
	@Override
	public String getErrorPath() {
		return "/error";
	}

	@RequestMapping
	public Object error(HttpServletRequest request, HttpServletResponse response) {

		if (WebUtils.isAjaxRequest(request) || WebUtils.isApiRequest(request)) {
			return ajaxError(request, response);
		}

		HttpStatus status = getStatus(request);
		if (!status.isError()) {
			return new ModelAndView("redirect:/");
		}
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> errors = errorAttributes.getErrorAttributes(new ServletWebRequest(request),
				ErrorAttributeOptions.defaults());
		if (!CollectionUtils.isEmpty(errors)) {
			map.putAll(errors);
		}

		if (!map.containsKey("status")) {
			map.put("status", status.value());
		}

		Object viewName = map.remove("viewName");
		if (viewName == null) {
			viewName = "error/" + status.value();
		}
		return new ModelAndView(viewName.toString(), map);
	}

	private ResponseEntity<Map<String, Object>> ajaxError(HttpServletRequest request, HttpServletResponse response) {
		HttpStatus status = getStatus(request);
		if (!status.isError()) {
			return new ResponseEntity<>(status);
		}
		Map<String, Object> attributes = errorAttributes.getErrorAttributes(new ServletWebRequest(request),
				ErrorAttributeOptions.defaults());
		return CollectionUtils.isEmpty(attributes) ? new ResponseEntity<>(status)
				: new ResponseEntity<>(attributes, status);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (status == null) {
			// if it's forward
			if (request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) != null) {
				status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			} else {
				status = HttpServletResponse.SC_OK;
			}
		}
		try {
			return HttpStatus.valueOf(status);
		} catch (IllegalArgumentException e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

}
