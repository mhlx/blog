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
package me.qyh.blog.template.render.thymeleaf;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;
import org.thymeleaf.expression.IExpressionObjects;
import org.thymeleaf.spring5.expression.IThymeleafEvaluationContext;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;

public class NoRestrictedEvaluationContext implements IThymeleafEvaluationContext {
	private final ThymeleafEvaluationContext evaluationContext;

	public NoRestrictedEvaluationContext(ApplicationContext applicationContext, ConversionService conversionService) {
		this.evaluationContext = new ThymeleafEvaluationContext(applicationContext, conversionService);
		this.evaluationContext.setVariableAccessRestricted(false);
	}

	@Override
	public TypedValue getRootObject() {
		return evaluationContext.getRootObject();
	}

	@Override
	public List<ConstructorResolver> getConstructorResolvers() {
		return evaluationContext.getConstructorResolvers();
	}

	@Override
	public List<MethodResolver> getMethodResolvers() {
		return evaluationContext.getMethodResolvers();
	}

	@Override
	public List<PropertyAccessor> getPropertyAccessors() {
		return evaluationContext.getPropertyAccessors();
	}

	@Override
	public TypeLocator getTypeLocator() {
		return evaluationContext.getTypeLocator();
	}

	@Override
	public TypeConverter getTypeConverter() {
		return evaluationContext.getTypeConverter();
	}

	@Override
	public TypeComparator getTypeComparator() {
		return evaluationContext.getTypeComparator();
	}

	@Override
	public OperatorOverloader getOperatorOverloader() {
		return evaluationContext.getOperatorOverloader();
	}

	@Override
	public BeanResolver getBeanResolver() {
		return evaluationContext.getBeanResolver();
	}

	@Override
	public void setVariable(String name, Object value) {
		evaluationContext.setVariable(name, value);
	}

	@Override
	public Object lookupVariable(String name) {
		return evaluationContext.lookupVariable(name);
	}

	@Override
	public final boolean isVariableAccessRestricted() {
		return false;
	}

	@Override
	public void setVariableAccessRestricted(boolean restricted) {
		// do nothing
	}

	@Override
	public IExpressionObjects getExpressionObjects() {
		return evaluationContext.getExpressionObjects();
	}

	@Override
	public void setExpressionObjects(IExpressionObjects expressionObjects) {
		evaluationContext.setExpressionObjects(expressionObjects);
	}

	public ApplicationContext getApplicationContext() {
		return evaluationContext.getApplicationContext();
	}
}
