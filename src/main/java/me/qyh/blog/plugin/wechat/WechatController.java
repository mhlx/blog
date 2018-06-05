package me.qyh.blog.plugin.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.plugin.wechat.WechatSupport.Signature;

@Controller
public class WechatController {

	@Autowired
	private UrlHelper urlHelper;

	private final WechatSupport wechatSupport;

	public WechatController(WechatSupport wechatSupport) {
		super();
		this.wechatSupport = wechatSupport;
	}

	@GetMapping("wechat/jsConfig")
	@ResponseBody
	public Signature jsConfig(@RequestParam(name = "path", defaultValue = "") String path) {
		String clean = FileUtils.cleanPath(path);
		String url = clean.isEmpty() ? urlHelper.getUrl() : urlHelper.getUrl() + '/' + clean;
		return wechatSupport.createSignature(url);
	}

}
