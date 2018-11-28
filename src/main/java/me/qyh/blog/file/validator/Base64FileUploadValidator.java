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
