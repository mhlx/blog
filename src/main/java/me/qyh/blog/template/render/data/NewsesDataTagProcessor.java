package me.qyh.blog.template.render.data;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.config.ConfigServer;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.NewsService;
import me.qyh.blog.core.vo.NewsArchive;
import me.qyh.blog.core.vo.NewsArchivePageQueryParam;
import me.qyh.blog.core.vo.PageResult;

public class NewsesDataTagProcessor extends DataTagProcessor<PageResult<NewsArchive>> {

	@Autowired
	private NewsService newsService;
	@Autowired
	private ConfigServer configServer;

	public NewsesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected PageResult<NewsArchive> query(Attributes attributes) throws LogicException {
		NewsArchivePageQueryParam param = new NewsArchivePageQueryParam();
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
		attributes.getString("content").ifPresent(param::setContent);
		param.setAsc(attributes.getBoolean("asc").orElse(false));
		param.setPageSize(attributes.getInteger("pageSize").orElse(0));
		param.setCurrentPage(attributes.getInteger("currentPage").orElse(1));

		if (param.getCurrentPage() < 1) {
			param.setCurrentPage(1);
		}

		int pageSize = configServer.getGlobalConfig().getNewsPageSize();
		if (param.getPageSize() < 1 || param.getPageSize() > pageSize) {
			param.setPageSize(pageSize);
		}

		return newsService.queryNewsArchive(param);
	}

	@Override
	public List<String> getAttributes() {
		return List.of("ymd", "queryPrivate", "asc", "pageSize", "currentPage");
	}
}
