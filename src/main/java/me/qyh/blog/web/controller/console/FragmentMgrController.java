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
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("console/template/fragment")
public class FragmentMgrController extends BaseMgrController {

	@Autowired
	private TemplateService templateService;
	@Autowired
	private SpaceService spaceService;

	@GetMapping
	public String index() {
		return "console/template/fragment";
	}

	@GetMapping("new")
	public String newFragment(Model model) {
		model.addAttribute("fragment", new Fragment());
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "console/template/fragment_build";
	}

	@GetMapping(value = "edit/{id}")
	public String update(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		Optional<Fragment> optional = templateService.queryFragment(id);
		if (!optional.isPresent()) {
			ra.addFlashAttribute(Constants.ERROR, new Message("fragment.user.notExists", "自定义模板片段不存在"));
			return "redirect:/console/template/fragment";
		}
		Fragment fragment = optional.get();
		model.addAttribute("fragment", fragment);
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		return "console/template/fragment_build";
	}

}
