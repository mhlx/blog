package me.qyh.blog.dataprovider;

import org.springframework.stereotype.Component;

import me.qyh.blog.service.MomentService;

@Component
public class PrevMomentDataProvider extends PrevOrNextMomentDataProvider {

	public PrevMomentDataProvider(MomentService momentService) {
		super("prevMoment", momentService, true);
	}

}
