package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import me.qyh.blog.entity.Moment;
import me.qyh.blog.service.MomentService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

@Component
public class MomentDataProvider extends DataProviderSupport<Moment> {

	private final MomentService momentService;

	public MomentDataProvider(MomentService momentService) {
		super("moment");
		this.momentService = momentService;
	}

	@Override
	public Moment provide(Map<String, String> attributesMap) throws Exception {
		String idStr = attributesMap.get("id");
		if (idStr == null) {
			BindingResult br = createBindingResult(attributesMap);
			br.rejectValue("id", "NotNull", "动态id不能为空");
			throw new BindException(br);
		}
		int id;
		try {
			id = Integer.parseInt(idStr);
			return momentService.getMoment(id).orElse(null);
		} catch (NumberFormatException e) {
			throw new TypeMismatchException(idStr, Integer.class);
		}
	}
}
