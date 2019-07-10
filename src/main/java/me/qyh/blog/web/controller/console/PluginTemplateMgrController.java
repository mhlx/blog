package me.qyh.blog.web.controller.console;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.PluginHandlerRegistry;
import me.qyh.blog.template.entity.PluginTemplate;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("console/template/pluginTemplate")
public class PluginTemplateMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;

	@GetMapping
	public String index() {
		return "console/template/pluginTemplate";
	}

	@GetMapping("new")
	public String newPluginTemplate(Model model) {
		model.addAttribute("plugins", PluginHandlerRegistry.getPlugins());
		model.addAttribute("pluginTemplate", new PluginTemplate());
		return "console/template/pluginTemplate_build";
	}

	@GetMapping(value = "edit/{id}")
	public String update(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		Optional<PluginTemplate> optional = templateService.getPluginTemplate(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("pluginTemplate.notExists", "插件模板不存在"));
			return "redirect:/console/template/pluginTemplate";
		}
		model.addAttribute("plugins", PluginHandlerRegistry.getPlugins());
		PluginTemplate pluginTemplate = optional.get();
		model.addAttribute("pluginTemplate", pluginTemplate);
		return "console/template/pluginTemplate_build";
	}

}
