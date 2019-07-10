package me.qyh.blog.plugin.staticfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.plugin.staticfile.vo.FileContent;
import me.qyh.blog.web.controller.console.BaseMgrController;

@Controller
@RequestMapping("console/staticFile")
public class StaticFileMgrController extends BaseMgrController {

	@Autowired
	private StaticFileManager handler;

	@GetMapping
	public String index() {
		return "plugin/staticfile/index";
	}

	@GetMapping("edit")
	public String edit(@RequestParam("path") String path, Model model, RedirectAttributes ra) {
		try {
			FileContent fileContent = handler.getEditableFile(path);
			model.addAttribute("file", fileContent);

			String ext = fileContent.getExt().toLowerCase();
			if (ext.equals("htm")) {
				ext = "html";
			}
			return "plugin/staticfile/editor_" + ext;
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/console/staticFile";
		}
	}
}
