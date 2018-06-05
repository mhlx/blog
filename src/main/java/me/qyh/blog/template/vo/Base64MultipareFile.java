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
package me.qyh.blog.template.vo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.Validators;

public class Base64MultipareFile implements MultipartFile {

	private final String name;
	private final byte[] content;
	private final String contentType;

	private static final Message INVALID_BASE64 = new Message("base64ImageFile.upload.base64.invalid", "无效的图片编码");

	public Base64MultipareFile(String name, String base64) throws LogicException {
		this.name = name;
		String[] array = base64.split(",");
		if (array.length < 2) {
			throw new LogicException(INVALID_BASE64);
		}
		String data = array[0];
		// data:{contentType};base64,
		int fIndex = data.indexOf(':');
		int lIndex = data.indexOf(';');
		if (fIndex == -1 || lIndex == -1) {
			throw new LogicException(INVALID_BASE64);
		}

		String contentType = data.substring(fIndex + 1, lIndex);
		if (Validators.isEmptyOrNull(contentType, true)) {
			this.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		} else {
			this.contentType = contentType;
		}

		String content = array[1];
		if (Validators.isEmptyOrNull(content, true)) {
			throw new LogicException(INVALID_BASE64);
		}

		try {
			this.content = Base64.getDecoder().decode(content);
		} catch (IllegalArgumentException e) {
			throw new LogicException(INVALID_BASE64);
		}

	}

	@Override
	public String getName() {
		throw new SystemException("unsupport");
	}

	@Override
	public String getOriginalFilename() {
		return name;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return content;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		Files.write(dest.toPath(), content);
	}
}