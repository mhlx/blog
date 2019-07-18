package me.qyh.blog.file.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Resources;
import me.qyh.blog.core.util.Validators;

public class ProcessUtils {

	private ProcessUtils() {
		super();
	}

	private static String runProcess(ProcessBuilder builder, long time, TimeUnit unit, boolean redirectToFile)
			throws IOException {

		Path temp = null;
		try {
			if (redirectToFile) {
				temp = Files.createTempFile(null, null);
				File file = temp.toFile();
				builder.redirectOutput(file);
				builder.redirectError(file);
			}

			Process process;
			try {
				process = builder.start();
			} catch (IOException e) {
				throw new IOException(e.getMessage(), e);
			}

			wait(process, time, unit);

			int status = process.exitValue();
			if (status != 0) {

				if (redirectToFile) {
					throw new IOException("操作异常：" + builder.command() + "错误信息：" + new String(Files.readAllBytes(temp)));
				}

				StringBuilder errorMsg = new StringBuilder();
				try {
					errorMsg.append(Resources.read(process.getInputStream()));
					errorMsg.append(System.lineSeparator());
				} catch (IOException e) {
					throw new IOException("操作异常：" + builder.command() + "，读取信息失败：" + e.getMessage(), e);
				}
				try {
					errorMsg.append(Resources.read(process.getErrorStream()));
				} catch (IOException e) {
					throw new IOException("操作异常：" + builder.command() + "，读取错误信息失败：" + e.getMessage(), e);
				}
				String msg = errorMsg.toString();
				if (!Validators.isEmptyOrNull(msg, true)) {
					throw new IOException("操作异常：" + builder.command() + "错误信息：" + msg);
				}
				throw new IOException("操作异常：" + builder.command() + "，未正确执行操作，状态码:" + status);
			}
			if (redirectToFile) {
				return new String(Files.readAllBytes(temp));
			}
			try {
				return Resources.read(process.getInputStream());
			} catch (IOException e) {
				throw new IOException("操作异常：" + builder.command() + "，读取信息失败：" + e.getMessage(), e);
			}
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

	/**
	 * 
	 * @param command
	 * @param time
	 * @param unit
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(List<String> command, long time, TimeUnit unit, boolean redirectToFile)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, time, unit, redirectToFile);
	}

	/**
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(List<String> command, boolean redirectToFile) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, -1, TimeUnit.SECONDS, redirectToFile);
	}

	/**
	 * 
	 * @param command
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(List<String> command, File directory, boolean redirectToFile) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(directory);
		return runProcess(builder, -1, TimeUnit.SECONDS, redirectToFile);
	}

	/**
	 * @param command
	 * @param time
	 * @param unit
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(List<String> command, long time, TimeUnit unit, File directory,
			boolean redirectToFile) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(directory);
		return runProcess(builder, time, unit, redirectToFile);
	}

	/**
	 * 
	 * @param command
	 * @param time
	 * @param unit
	 * @return
	 * @throws IOException
	 */
	public static String runProcess(String[] command, long time, TimeUnit unit, boolean redirectToFile)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		return runProcess(builder, time, unit, redirectToFile);
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
	 * @param time    如果为-1，一直等待
	 * @param unit
	 * @throws IOException
	 */
	public static void wait(Process process, long time, TimeUnit unit) throws IOException {
		try {
			if (time == -1) {
				process.waitFor();
			} else {
				if (!process.waitFor(time, unit)) {
					destory(process);
					throw new IOException("操作超时:" + process.toHandle().info().commandLine().orElse(""));
				}
			}
		} catch (InterruptedException e) {
			destory(process);
			Thread.currentThread().interrupt();
			throw new SystemException(e.getMessage(), e);
		}
	}
}
