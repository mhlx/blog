package me.qyh.blog.file;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import me.qyh.blog.Markdown2Html;

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
	public String renderMd(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (path == null) {
			throw new RuntimeException(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "属性为null");
		}
		Optional<FileInfoDetail> optionalFid = fileService.getFileInfoDetail(path);
		FileInfoDetail fid;
		if (optionalFid.isEmpty() || (fid = optionalFid.get()).isDir()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		model.addAttribute("content", markdown2Html.toHtml(fid.getContent()));
		return "markdown_render";
	}

}