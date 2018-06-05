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
package me.qyh.blog.template;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * <p>
 * 模板拦截器，等效于HandlerInterceptor
 * </p>
 * <p>
 * <b>无法拦截Fragment、非注册PathTemplate，因为它本质上还是基于路径的拦截器</b>
 * </p>
 * 
 * @author mhlx
 * 
 * @since 5.6
 *
 */
public interface TemplateInterceptor extends AsyncHandlerInterceptor {

	boolean match(String templateName, HttpServletRequest request);

}
