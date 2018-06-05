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
package me.qyh.blog.core.exception;

/**
 * 系统异常
 * <p>
 * 同时用来将checked exception转化为unchecked exception
 * </p>
 * 
 * @author Administrator
 *
 */
public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            异常信息
	 * @param cause
	 *            导致系统异常的异常
	 */
	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param message
	 *            异常信息
	 */
	public SystemException(String message) {
		super(message);
	}

}
