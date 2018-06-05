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
package me.qyh.blog.template.render;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import me.qyh.blog.core.exception.SystemException;

/**
 * 一个<b>只读的</b>HttpServletResponse
 * 
 * @author mhlx
 *
 */
public final class ReadOnlyResponse extends HttpServletResponseWrapper {

	public ReadOnlyResponse(HttpServletResponse response) {
		super(response);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 返回<b>ReadOnlyResponse</b>
	 * </p>
	 */
	@Override
	public ServletResponse getResponse() {
		return this;
	}

	@Override
	public void setResponse(ServletResponse response) {
		unsupport();
	}

	@Override
	public void setCharacterEncoding(String charset) {
		unsupport();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		unsupport();
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		unsupport();
		return null;
	}

	@Override
	public void setContentLength(int len) {
		unsupport();
	}

	@Override
	public void setContentLengthLong(long len) {
		unsupport();
	}

	@Override
	public void setContentType(String type) {
		unsupport();
	}

	@Override
	public void setBufferSize(int size) {
		unsupport();
	}

	@Override
	public void flushBuffer() throws IOException {
		unsupport();
	}

	@Override
	public void reset() {
		unsupport();
	}

	@Override
	public void resetBuffer() {
		unsupport();
	}

	@Override
	public void setLocale(Locale loc) {
		unsupport();
	}

	@Override
	public void addCookie(Cookie cookie) {
		unsupport();
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		unsupport();
	}

	@Override
	public void sendError(int sc) throws IOException {
		unsupport();
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		unsupport();
	}

	@Override
	public void setDateHeader(String name, long date) {
		unsupport();
	}

	@Override
	public void addDateHeader(String name, long date) {
		unsupport();
	}

	@Override
	public void setHeader(String name, String value) {
		unsupport();
	}

	@Override
	public void addHeader(String name, String value) {
		unsupport();
	}

	@Override
	public void setIntHeader(String name, int value) {
		unsupport();
	}

	@Override
	public void addIntHeader(String name, int value) {
		unsupport();
	}

	@Override
	public void setStatus(int sc) {
		unsupport();
	}

	@Deprecated
	@Override
	public void setStatus(int sc, String sm) {
		unsupport();
	}

	private void unsupport() {
		throw new SystemException("不支持这个方法");
	}

}