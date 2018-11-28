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
