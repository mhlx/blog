package me.qyh.blog.template.render.data;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.vo.ArticleArchive;
import me.qyh.blog.core.vo.ArticleArchivePageQueryParam;
import me.qyh.blog.core.vo.PageResult;

/**
 * 文章归档
 * 
 * @author Administrator
 *
 */
public class ArticleArchivesDataTagProcessor extends DataTagProcessor<PageResult<ArticleArchive>> {

	@Autowired
	private ArticleService articleService;
	@Autowired
	private ConfigServer configServer;

	public ArticleArchivesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<ArticleArchive> query(Attributes attributes) throws LogicException {
		ArticleArchivePageQueryParam param = new ArticleArchivePageQueryParam();
		String ymd = attributes.getString("ymd").orElse(null);
		if (ymd != null) {
			try {
				LocalDate.parse(ymd);
			} catch (DateTimeParseException e) {
				ymd = null;
			}
		}
		param.setYmd(ymd);
		param.setQueryPrivate(attributes.getBoolean("queryPrivate").orElse(true));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		param.setIgnorePaging(attributes.getBoolean("ignorePaging").orElse(true));
		int pageSize = configServer.getGlobalConfig().getArticleArchivePageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(1));
		return articleService.queryArticleArchives(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("currentPage", "pageSize", "queryPrivate", "ymd", "ignorePaging");
	}

}
