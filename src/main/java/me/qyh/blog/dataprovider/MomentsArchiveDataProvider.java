package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import me.qyh.blog.service.MomentService;
import me.qyh.blog.vo.MomentArchive;
import me.qyh.blog.vo.MomentArchiveQueryParam;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class MomentsArchiveDataProvider extends DataProviderSupport<PageResult<MomentArchive>> {

	private final MomentService momentService;

	public MomentsArchiveDataProvider(MomentService momentService) {
		super("momentArchivePage");
		this.momentService = momentService;
	}

	@Override
	public PageResult<MomentArchive> provide(Map<String, String> attributesMap) throws BindException {
		MomentArchiveQueryParam param = bindQueryParam(attributesMap);
		return momentService.queryMomentArchive(param);
	}

	private MomentArchiveQueryParam bindQueryParam(Map<String, String> attributesMap) throws BindException {
		return bind(new MomentArchiveQueryParam(), attributesMap);
	}
}
