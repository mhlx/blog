package me.qyh.blog.web.controller.console;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.qyh.blog.core.plugin.IconRegistry;

@Controller
@RequestMapping("console/plugin")
public class PluginMgrController extends BaseMgrController {

	@GetMapping
	public String index(Model model) {
		model.addAttribute("icons", IconRegistry.getInstance().getIcons());
		return "console/plugin/index";
	}

}
