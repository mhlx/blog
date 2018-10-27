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
package me.qyh.blog.file.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.vo.Base64FileUpload;

@Component
public class Base64FileUploadValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Base64FileUpload.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Base64FileUpload upload = (Base64FileUpload) target;
		String name = upload.getName();
		String base64 = upload.getBase64();

		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("base64File.upload.name.blank", "文件名为空");
			return;
		}

		if (Validators.isEmptyOrNull(base64, true)) {
			errors.reject("base64File.upload.base64.blank", "图片内容不能为空");
			return;
		}
	}

}
