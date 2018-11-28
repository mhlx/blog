package me.qyh.blog.template.render.thymeleaf.dialect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.UrlUtils;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.render.RedirectException;

/**
 * 用于跳转页
 * <p>
 * 这个标签应该尽早的出现，因为它的出现意味着以前所有的解析都是无效的。 <br>
 * <b>应该谨慎的使用这个标签，错误的使用它可能会陷入无限的重定向循环，而且尽可能的用它做单一跳转，而不是连续的跳转(浏览器会对此做限制)</b>
 * </p>
 * <p>
 * <b>如果callable fragment中有该标签，那么ajax请求将会返回RedirectJsonResult，而不会返回目标页面内容</b>
 * <br>
 * </p>
 *
 * @see RedirectException
 */
public class RedirectTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "redirect";
	private static final int PRECEDENCE = 1000;
	private static final String URL_ATTR = "url";
	// 是否是301跳转
	private static final String MOVED_PERMANENTLY_ATTR = "permanently";
	private static final String CODE_ATTR = "code";
	private static final String ARGUMENT_SPLIT_ATTR = "argumentSpliter";
	private static final String ARGUMENTS_ATTR = "arguments";
	private static final String DEFAULT_MSG_ATTR = "defaultMsg";

	private final UrlHelper urlHelper;

	public RedirectTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
		this.urlHelper = applicationContext.getBean(UrlHelper.class);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {

		structureHandler.removeElement();

		Map<String, String> attMap = processAttribute(context, tag);

		String url = attMap.get(URL_ATTR);
		if (url == null) {
			return;
		}

		String redirectUrl = url;
		if (!UrlUtils.isAbsoluteUrl(redirectUrl)) {
			redirectUrl = urlHelper.getUrl() + "/" + FileUtils.cleanPath(redirectUrl);
		}

		URL _url = null;
		try {
			_url = new URL(redirectUrl);
		} catch (MalformedURLException e) {
			// invalid url
			// ignore
		}

		if (_url != null) {
			RedirectException ex = new RedirectException(_url.toString(),
					Boolean.parseBoolean(attMap.get(MOVED_PERMANENTLY_ATTR)));
			if (!ex.isPermanently()) {
				String code = attMap.get(CODE_ATTR);
				if (!Validators.isEmptyOrNull(code, true)) {
					String defaultMsg = attMap.get(DEFAULT_MSG_ATTR);
					String[] argumentsArray = {};
					String arguments = attMap.get(ARGUMENTS_ATTR);
					String argumentSpliter = attMap.getOrDefault(ARGUMENT_SPLIT_ATTR, ",");
					if (arguments != null) {
						argumentsArray = arguments.split(argumentSpliter);
					}
					ex.setRedirectMsg(new Message(code, defaultMsg, (Object[]) argumentsArray));
				}
			}
			throw ex;
		}
	}

}
