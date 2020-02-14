package me.qyh.blog.support.wechat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.exception.BadRequestException;
import me.qyh.blog.support.wechat.WechatSupport.Signature;
import me.qyh.blog.utils.WebUtils;

@Controller
@RequestMapping("support/wechat")
@ConditionalOnProperty(prefix = "blog.support.wechat", name = { "appid", "appsecret" })
public class WechatController {

	private final WechatSupport WechatSupport;

	public WechatController(WechatSupport wechatSupport) {
		super();
		WechatSupport = wechatSupport;
	}

	@ResponseBody
	@GetMapping("jsConfig")
	public Signature jsConfig(@RequestParam(name = "url") String url) {
		if (!WebUtils.isAbsoluteWebUrl(url)) {
			// we need an absolute web url
			throw new BadRequestException("wechar.jsConfig.url.Invalid", "url必须是一个完整的web地址");
		}
		return WechatSupport.createSignature(url);
	}

}
