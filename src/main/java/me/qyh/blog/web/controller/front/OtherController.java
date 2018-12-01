package me.qyh.blog.web.controller.front;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.template.render.Fragments;
import me.qyh.blog.template.render.ParseConfig;
import me.qyh.blog.template.render.ReadOnlyResponse;
import me.qyh.blog.template.render.TemplateRender;
import me.qyh.blog.template.render.TemplateRenderException;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.validator.FragmentValidator;
import me.qyh.blog.template.vo.DataBind;
import me.qyh.blog.template.vo.DataTag;

@RestController
public class OtherController {

	@Autowired
	private TemplateRender templateRender;
	@Autowired
	private TemplateService templateService;

	@GetMapping({ "api/data/{tagName}", "space/{alias}/api/data/{tagName}" })
	public ResponseEntity<Object> queryData(@PathVariable("tagName") String tagName,
			@RequestParam Map<String, String> allRequestParams, HttpServletRequest request,
			HttpServletResponse response) throws LogicException {
		try {
			tagName = URLDecoder.decode(tagName, Constants.CHARSET.name());
		} catch (UnsupportedEncodingException e) {
			throw new LogicException("data.name.undecode", "无法解码的数据名称");
		}
		DataTag tag = new DataTag(tagName, new HashMap<>(allRequestParams));
		Optional<DataBind> op = templateService.queryData(tag, true);
		if (op.isPresent()) {
			DataBind bind = op.get();
			Object data = bind.getData();
			if (data == null) {
				return ResponseEntity.noContent().build();
			} else {
				return ResponseEntity.ok(data);
			}
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping({ "api/fragment/{fragment}", "space/{alias}/api/fragment/{fragment}" })
	public ResponseEntity<String> queryFragment(@PathVariable("fragment") String fragment,
			@RequestParam Map<String, String> allRequestParams, HttpServletRequest request,
			HttpServletResponse response) throws LogicException, TemplateRenderException {
		try {
			fragment = FragmentValidator.validName(fragment, true);
		} catch (LogicException e) {
			throw new LogicException("data.name.undecode", "无法解码的数据名称");
		}
		try {

			String templateName = Fragments.getCurrentTemplateName(fragment);
			String content = templateRender.doRender(templateName, null, request, new ReadOnlyResponse(response),
					new ParseConfig(true));
			return ResponseEntity.ok(content);

		} catch (TemplateRenderException e) {
			throw e;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new SystemException(e.getMessage(), e);
		}
	}
}
