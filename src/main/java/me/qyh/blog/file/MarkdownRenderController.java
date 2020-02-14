package me.qyh.blog.file;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import me.qyh.blog.Markdown2Html;
import me.qyh.blog.exception.ResourceNotFoundException;

@Controller
@RequestMapping
@Conditional(FileCondition.class)
public class MarkdownRenderController {

	private final Markdown2Html markdown2Html;
	private final FileService fileService;

	public MarkdownRenderController(Markdown2Html markdown2Html, FileService fileService) {
		this.markdown2Html = markdown2Html;
		this.fileService = fileService;
	}

	@GetMapping("/**/*.md")
	public String renderMd(HttpServletRequest request, Model model) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (path == null) {
			throw new RuntimeException(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "属性为null");
		}
		FileInfoDetail fid = fileService.getFileInfoDetail(path);
		if (fid.isDir()) {
			throw new ResourceNotFoundException("file.notExists", "文件不存在");
		}
		model.addAttribute("content", markdown2Html.toHtml(fid.getContent()));
		return "console/file/render/markdown";
	}

}