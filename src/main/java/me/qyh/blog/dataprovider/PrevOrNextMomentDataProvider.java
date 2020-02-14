package me.qyh.blog.dataprovider;

import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import me.qyh.blog.entity.Moment;
import me.qyh.blog.service.MomentService;
import me.qyh.blog.web.template.tag.DataProviderSupport;

class PrevOrNextMomentDataProvider extends DataProviderSupport<Moment> {
	private final MomentService momentService;
	private final boolean prev;

	public PrevOrNextMomentDataProvider(String name, MomentService momentService, boolean prev) {
		super(name);
		this.momentService = momentService;
		this.prev = prev;
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
		} catch (NumberFormatException e) {
			throw new TypeMismatchException(idStr, Integer.class);
		}

		if (prev) {
			return momentService.prev(id).orElse(null);
		}

		return momentService.next(id).orElse(null);
	}
}
