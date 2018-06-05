package me.qyh.blog.plugin.sitemap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.plugin.sitemap.component.SiteMapSupport;

@Controller
public class SiteMapController {

	@Autowired
	private SiteMapSupport support;

	@GetMapping(value = "sitemap.xml", produces = "application/xml;charset=utf8")
	@ResponseBody
	public String sitemap() {
		return support.getSiteMapXml();
	}

}
