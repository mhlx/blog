/*
 * Copyright 2018 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.template.render.thymeleaf.dialect;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;

/**
 * 用来限制页面只能在一个时间段内被访问
 */
public class PeriodTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "period";
	private static final int PRECEDENCE = 1000;

	private static final String BEGIN = "begin";
	private static final String END = "end";
	private static final String INCLUDE = "include";

	public PeriodTagProcessor(String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		Map<String, String> attMap = processAttribute(context, tag);

		LocalDateTime beginTime = null;
		LocalDateTime endTime = null;

		String begin = attMap.get(BEGIN);
		if (!Validators.isEmptyOrNull(begin, true)) {
			beginTime = Times.parseAndGet(begin);
		}

		String end = attMap.get(END);
		if (!Validators.isEmptyOrNull(end, true)) {
			endTime = Times.parseAndGet(end);
		}

		boolean include = Boolean.parseBoolean(attMap.getOrDefault(INCLUDE, "true"));

		boolean removed = false;

		try {
			PeriodStatus status = parse(beginTime, endTime, include);
			Objects.requireNonNull(status);

			if (PeriodStatus.IN.equals(status)) {
				structureHandler.removeTags();
				removed = true;
			}

		} finally {
			if (!removed) {
				structureHandler.removeElement();
			}
		}
	}

	/**
	 * @return NOT NULL !!
	 */
	protected PeriodStatus parse(LocalDateTime beginTime, LocalDateTime endTime, boolean include) {
		if (beginTime == null || endTime == null) {
			return PeriodStatus.INVALID;
		}
		LocalDateTime now = Times.now();

		boolean invalid = endTime.isBefore(beginTime) || endTime.equals(beginTime);

		if (invalid) {
			return PeriodStatus.INVALID;
		}

		if (now.isBefore(beginTime) || now.isAfter(endTime)) {
			return include ? PeriodStatus.OUT : PeriodStatus.IN;
		}

		if ((now.isAfter(beginTime) && now.isBefore(endTime)) || now.isEqual(beginTime) || now.isEqual(endTime)) {
			return include ? PeriodStatus.IN : PeriodStatus.OUT;
		}

		throw new TemplateProcessingException("无法判断的时间范围:[" + beginTime + "," + endTime + "]");
	}

	protected enum PeriodStatus {
		IN, OUT, INVALID
	}
}
