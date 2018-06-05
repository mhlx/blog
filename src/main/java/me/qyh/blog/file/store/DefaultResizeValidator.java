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
package me.qyh.blog.file.store;

import java.util.Arrays;

/**
 * 默认缩放校验器
 * 
 * @author Administrator
 *
 */
public class DefaultResizeValidator implements ResizeValidator {

	private Integer[] allowSizes;

	/**
	 * default
	 */
	public DefaultResizeValidator() {
		super();
	}

	/**
	 * 
	 * @param allowSizes
	 *            允许的尺寸
	 */
	public DefaultResizeValidator(Integer[] allowSizes) {
		super();
		this.allowSizes = allowSizes;
	}

	@Override
	public boolean valid(Resize resize) {
		if (resize == null) {
			return false;
		}
		if (allowSizes != null && (resize.getSize() == null || !inSize(resize.getSize()))) {
			return false;
		}
		if (resize.getSize() != null) {
			return resize.getSize() > 0;
		} else {
			if (resize.getWidth() <= 0 && resize.getHeight() <= 0) {
				return false;
			}
			// 如果没有指定纵横比但是没有指定长宽
			return resize.isKeepRatio() || (resize.getWidth() > 0 && resize.getHeight() > 0);
		}
	}

	private boolean inSize(int size) {
		return Arrays.stream(allowSizes).anyMatch(allowSize -> allowSize == size);
	}

	public void setAllowSizes(Integer[] allowSizes) {
		this.allowSizes = allowSizes;
	}
}