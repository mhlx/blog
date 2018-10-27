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

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.file.vo.BlogFileUpload;

@Component
public class BlogFileUploadValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return BlogFileUpload.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		BlogFileUpload upload = (BlogFileUpload) target;
		List<MultipartFile> files = upload.getFiles();
		if (CollectionUtils.isEmpty(files)) {
			errors.reject("file.upload.files.blank", "需要上传文件为空");
			return;
		}
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				String originalFilename = file.getOriginalFilename();
				errors.reject("file.upload.content.blank", new Object[] { originalFilename },
						"文件" + originalFilename + "内容不能为空");
				return;
			}
		}
	}

}
