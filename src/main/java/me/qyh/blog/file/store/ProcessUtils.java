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
package me.qyh.blog.file.store;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.file.store.local.ProcessException;

public class ProcessUtils {

	private ProcessUtils() {
		super();
	}

	private static Optional<String> runProcess(ProcessBuilder builder, long time, TimeUnit unit)
			throws ProcessException {
		Process process;
		try {
			process = builder.start();
		} catch (IOException e) {
			throw new ProcessException(e.getMessage(), e);
		}

		wait(process, time, unit);

		int status = process.exitValue();
		if (status != 0) {
			StringBuilder errorMsg = new StringBuilder();
			try {
				Resources.read(process.getInputStream()).ifPresent(s -> {
					errorMsg.append(s);
					errorMsg.append(System.lineSeparator());
				});
			} catch (IOException e) {
				throw new ProcessException("操作异常：" + builder.command() + "，读取信息失败：" + e.getMessage(), e);
			}
			try {
				Resources.read(process.getErrorStream()).ifPresent(s -> {
					errorMsg.append(s);
				});
			} catch (IOException e) {
				throw new ProcessException("操作异常：" + builder.command() + "，读取错误信息失败：" + e.getMessage(), e);
			}
			String msg = errorMsg.toString();
			if (!Validators.isEmptyOrNull(msg, true)) {
				throw new ProcessException("操作异常：" + builder.command() + "错误信息：" + msg);
			}
			throw new ProcessException("操作异常：" + builder.command() + "，未正确执行操作，状态码:" + status);
		}
		try {
			return Resources.read(process.getInputStream());
		} catch (IOException e) {
			throw new ProcessException("操作异常：" + builder.command() + "，读取信息失败：" + e.getMessage(), e);
		}
	}

	/**
	 * <b>PIPE ONLY</b>
	 * 
	 * @param command
	 * @param time
	 * @param unit
	 * @return
	 * @throws ProcessException
	 */
	public static Optional<String> runProcess(List<String> command, long time, TimeUnit unit) throws ProcessException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, time, unit);
	}

	/**
	 * <b>PIPE ONLY</b>
	 * 
	 * @param command
	 * @return
	 * @throws ProcessException
	 */
	public static Optional<String> runProcess(List<String> command) throws ProcessException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, -1, TimeUnit.SECONDS);
	}

	/**
	 * <b>PIPE ONLY</b>
	 * 
	 * @param command
	 * @param directory
	 * @return
	 * @throws ProcessException
	 */
	public static Optional<String> runProcess(List<String> command, File directory) throws ProcessException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(directory);
		return runProcess(builder, -1, TimeUnit.SECONDS);
	}

	/**
	 * <b>PIPE ONLY</b>
	 * 
	 * @param command
	 * @param time
	 * @param unit
	 * @param directory
	 * @return
	 * @throws ProcessException
	 */
	public static Optional<String> runProcess(List<String> command, long time, TimeUnit unit, File directory)
			throws ProcessException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(directory);
		return runProcess(builder, time, unit);
	}

	/**
	 * <b>PIPE ONLY</b>
	 * 
	 * @param command
	 * @param time
	 * @param unit
	 * @return
	 * @throws ProcessException
	 */
	public static Optional<String> runProcess(String[] command, long time, TimeUnit unit) throws ProcessException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, time, unit);
	}

	private static void destory(Process p) {
		ProcessHandle handle = p.toHandle();
		handle.destroy();
		handle.descendants().forEach(ProcessHandle::destroy);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (p.isAlive()) {
			handle.destroyForcibly();
			handle.descendants().forEach(ProcessHandle::destroyForcibly);
		}
	}

	/**
	 * 等待进程退出
	 * 
	 * @param process
	 * @param time
	 *            如果为-1，一直等待
	 * @param unit
	 * @throws ProcessException
	 */
	public static void wait(Process process, long time, TimeUnit unit) throws ProcessException {
		try {
			if (time == -1) {
				process.waitFor();
			} else {
				if (!process.waitFor(time, unit)) {
					destory(process);
					throw new ProcessException("操作超时:" + process.toHandle().info().commandLine().orElse(""));
				}
			}
		} catch (InterruptedException e) {
			destory(process);
			Thread.currentThread().interrupt();
			throw new SystemException(e.getMessage(), e);
		}
	}
}
