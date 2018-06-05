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

/**
 * 缩放尺寸
 * 
 * @author Administrator
 *
 */
public class Resize {

	private int width;// 缩略图宽度
	private int height;// 缩略图高度
	private boolean keepRatio = true;// 保持纵横比
	private Integer size;// 如果设置了该属性，其他属性将失效

	/**
	 * default
	 */
	public Resize() {
		super();
	}

	/**
	 * 
	 * @param size
	 *            缩放尺寸
	 */
	public Resize(Integer size) {
		this.size = size;
	}

	/**
	 * 
	 * @param width
	 *            宽
	 * @param height
	 *            高
	 * @param keepRatio
	 *            是否保持纵横比
	 */
	public Resize(int width, int height, boolean keepRatio) {
		this.width = width;
		this.height = height;
		this.keepRatio = keepRatio;
	}

	/**
	 * @param width
	 *            宽
	 * @param height
	 *            高
	 */
	public Resize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public Integer getSize() {
		return size;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isKeepRatio() {
		return keepRatio;
	}

	public void setKeepRatio(boolean keepRatio) {
		this.keepRatio = keepRatio;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public String toString() {
		if (size != null) {
			return "size=" + size;
		} else {
			StringBuilder sb = new StringBuilder();
			if (width > 0) {
				sb.append("width=").append(width).append(",");
			}
			if (height > 0) {
				sb.append("height=").append(height).append(",");
			}
			sb.append("keepRatio=").append(keepRatio);
			return sb.toString();
		}
	}
}
