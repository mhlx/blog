/*
 * Copyright 2016 qyh.me
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

import java.io.Writer;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.FastStringWriter;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.template.render.Fragments;
import me.qyh.blog.template.validator.FragmentValidator;

/**
 * Fragment 标签处理器，fragment标签和
 * th:insert|th:replace的最主要区别在于，th:insert|th:replace的缓存和页面关联，而fragment缓存与页面无关
 * 
 * 
 * {@link https://github.com/thymeleaf/thymeleaf/issues/515}
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * 
 * @author mhlx
 *
 */
public class FragmentTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "fragment";
	private static final int PRECEDENCE = 1000;
	private static final String NAME = "name";

	public FragmentTagProcessor(String dialectPrefix, ApplicationContext ctx) {
		super(TemplateMode.HTML, // This processor will apply only to HTML mode
				dialectPrefix, // Prefix to be applied to name for matching
				TAG_NAME, // Tag name: match specifically this tag
				false, // Apply dialect prefix to tag name
				null, // No attribute name: will match by tag name
				false, // No prefix to be applied to attribute name
				PRECEDENCE); // Precedence (inside dialect's own precedence)
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		Map<String, String> attMap = processAttribute(context, tag);
		String name = attMap.get(NAME);
		if (name != null) {

			try {
				FragmentValidator.validName(name, false);
			} catch (LogicException e) {
				structureHandler.removeElement();
				return;
			}

			String templateName = Fragments.getCurrentTemplateName(name);

			Writer writer = new FastStringWriter(200);

			context.getConfiguration().getTemplateManager()
					.parseAndProcess(new TemplateSpec(templateName, TemplateMode.HTML), context, writer);
			structureHandler.replaceWith(writer.toString(), false);
			return;
		}
		structureHandler.removeElement();
	}

}
