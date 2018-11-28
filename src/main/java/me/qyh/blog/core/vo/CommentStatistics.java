package me.qyh.blog.core.vo;

import java.util.ArrayList;
import java.util.List;

import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.template.service.TemplateService;

public class CommentStatistics {

	private final List<CommentModuleStatistics> modules = new ArrayList<>();

	public void addModule(CommentModuleStatistics st) {
		this.modules.add(st);
	}

	public List<CommentModuleStatistics> getModules() {
		return modules;
	}

	/**
	 * 适配以前
	 * 
	 * @return
	 */
	public int getTotalArticleComments() {
		return getComments(ArticleService.COMMENT_MODULE_NAME);
	}

	/**
	 * 适配以前
	 * 
	 * @return
	 */
	public int getTotalPageComments() {
		return getComments(TemplateService.COMMENT_MODULE_NAME);
	}

	public int getNewsComments() {
		return getComments(NewsService.COMMENT_MODULE_NAME);
	}

	public int getComments(String type) {
		for (CommentModuleStatistics module : modules) {
			if (module.getType().equals(type)) {
				return module.getCount();
			}
		}
		return 0;
	}

}
