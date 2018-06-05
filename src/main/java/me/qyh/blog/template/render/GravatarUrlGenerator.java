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
package me.qyh.blog.template.render;

/**
 * <p>
 * 用于生成头像的md5地址
 * </p>
 * 
 * <pre>
 * usage:
 * <code>
 * ${gravatars.getUrl('emailMD5')}
 *</code>
 * </pre>
 * 
 * @since 5.9
 * 
 *
 */
public interface GravatarUrlGenerator {

	String getUrl(String md5);

}
