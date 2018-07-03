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

import static me.qyh.blog.template.render.data.DataTagProcessor.validDataName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.LazyContextVariable;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.render.ParseContextHolder;
import me.qyh.blog.template.render.ParsedTemplate;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.template.vo.DataBind;
import me.qyh.blog.template.vo.DataTag;

/**
 * {@link http://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html#creating-our-own-dialect}
 * 
 * @author mhlx
 *
 */
public class DataTagProcessor extends DefaultAttributesTagProcessor {

	private static final String TAG_NAME = "data";
	private static final String ALIAS = "alias";
	private static final int PRECEDENCE = 1000;
	private static final String NAME_ATTR = "name";

	/**
	 * 使用变量时才加载
	 * 
	 * @since 6.5
	 */
	private static final String LAZY = "lazy";

	/**
	 * <p>
	 * 例如
	 * 
	 * <pre>
	 * &lt;data name="articleNav" ref-article2="article"/&gt;
	 * </pre>
	 * 
	 * 将会从 通过 request.getAttrbutes('article')取得值，并将值通过 tagAttrMap.put(article2,v)来传递
	 * </p>
	 * 
	 * @since 6.4
	 */
	private static final String REF_PREFIX = "ref-";

	private final TemplateService templateService;

	public DataTagProcessor(String dialectPrefix, ApplicationContext applicationContext) {
		super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
		this.templateService = applicationContext.getBean(TemplateService.class);
	}

	@Override
	protected final void doProcess(ITemplateContext context, IProcessableElementTag tag,
			IElementTagStructureHandler structureHandler) {
		try {

			Map<String, String> attMap = processAttribute(context, tag);
			String name = attMap.get(NAME_ATTR);
			if (Validators.isEmptyOrNull(name, true)) {
				return;
			}

			String alias = attMap.get(ALIAS);

			boolean hasAlias = !Validators.isEmptyOrNull(alias, true);
			if (hasAlias && !validDataName(alias)) {
				throw new TemplateProcessingException("dataName必须为英文字母或者数字，并且不能以数字开头");
			}

			Map<String, Object> tagAttMap = new HashMap<>(attMap);

			IWebContext webContext = (IWebContext) context;
			HttpServletRequest request = webContext.getRequest();

			Iterator<Map.Entry<String, String>> iter = attMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				if (key.startsWith(REF_PREFIX)) {
					if (key.equals(REF_PREFIX)) {
						throw new SystemException(REF_PREFIX + "必须带有属性名称");
					}
					String ref = entry.getValue();
					Object v = request.getAttribute(ref);
					if (v == null) {
						throw new TemplateProcessingException("没有在request属性中找到" + ref);
					}
					String refAttr = key.substring(4);
					if (tagAttMap.containsKey(refAttr)) {
						throw new TemplateProcessingException("标签属性名中已经存在" + refAttr + "了，请通过ref-attr重新指定新的名称");
					}
					tagAttMap.put(refAttr, v);
					iter.remove();
				}
			}
			DataTag dataTag = new DataTag(name, tagAttMap);

			Optional<DataBind> optional = queryDataBind(dataTag);
			optional.ifPresent(dataBind -> {
				DataBind bind = dataBind;
				if (hasAlias) {
					bind.setDataName(alias);
				}
				if (request.getAttribute(bind.getDataName()) != null) {
					throw new TemplateProcessingException("属性" + bind.getDataName() + "已经存在于request中");
				}
				if (Boolean.parseBoolean(attMap.get(LAZY))) {
					request.setAttribute(bind.getDataName(), new LazyContextVariable<Object>() {

						@Override
						protected Object loadValue() {
							return getData(bind);
						}

					});
				} else {
					request.setAttribute(bind.getDataName(), getData(bind));
				}
			});
		} finally {
			structureHandler.removeElement();
		}
	}

	private Optional<DataBind> queryDataBind(DataTag dataTag) {
		return templateService.queryData(dataTag, ParseContextHolder.getContext().isOnlyCallable()
				&& !ParseContextHolder.getContext().getRoot().map(ParsedTemplate::isCallable).orElse(false));
	}

	private Object getData(DataBind bind) {
		try {
			return bind.getData();
		} catch (LogicException e) {
			throw new RuntimeLogicException(e);
		}
	}

}
