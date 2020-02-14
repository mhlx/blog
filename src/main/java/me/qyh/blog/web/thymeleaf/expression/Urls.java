package me.qyh.blog.web.thymeleaf.expression;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.WebEngineContext;

import me.qyh.blog.web.template.TemplateUtils;

/**
 * NOT thread safe
 * 
 * @author wwwqyhme
 *
 */
final class Urls {

	private final HttpServletRequest request;
	private final IExpressionContext context;
	private final Map<String, String> urlExpressionMap;
	private static final String OBJECT_NAME = "o";

	private static final Map<String, String> globalUrlExpressionMap;

	static {
		globalUrlExpressionMap = Map.of("Article", "'/articles/'+${o.alias == null ? o.id : o.alias}", "Moment",
				"'/moments/'+${o.id}", "Template", "${o.definitelyPattern ? o.pattern : null}");
	}

	private static final String URL_EXPRESSION_ATTRIBUTE = TemplateUtils.class.getName() + ".URL_EXPRESSION_MAP";

	@SuppressWarnings("unchecked")
	public Urls(HttpServletRequest request, IExpressionContext context) {
		super();
		this.request = request;
		this.context = context;
		Map<String, String> map = (Map<String, String>) request.getAttribute(URL_EXPRESSION_ATTRIBUTE);
		if (map == null) {
			map = new HashMap<>(globalUrlExpressionMap);
			request.setAttribute(URL_EXPRESSION_ATTRIBUTE, map);
		}
		this.urlExpressionMap = map;
	}

	public ServletUriComponentsBuilder builder(boolean withQueryString) {
		return withQueryString ? ServletUriComponentsBuilder.fromRequest(request)
				: ServletUriComponentsBuilder.fromRequestUri(request);
	}

	public String getUrl(Object o) {
		if (o == null || urlExpressionMap == null) {
			return null;
		}
		String expression = urlExpressionMap.get(o.getClass().getSimpleName());
		if (expression == null) {
			return null;
		}
		WebEngineContext webContext = (WebEngineContext) context;
		webContext.setVariable(OBJECT_NAME, o);
		try {
			return TemplateUtils.processExpression(expression, webContext);
		} finally {
			webContext.removeVariable(OBJECT_NAME);
		}
	}

	public void setUrlExpression(String name, String expression) {
		if (urlExpressionMap != null) {
			urlExpressionMap.put(name, expression);
		}
	}

}
