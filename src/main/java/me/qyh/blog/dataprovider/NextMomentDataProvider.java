package me.qyh.blog.dataprovider;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.MomentService;

@Component
public class NextMomentDataProvider extends PrevOrNextMomentDataProvider {

	public NextMomentDataProvider(MomentService momentService) {
		super("nextMoment", momentService, false);
	}

}
