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
package me.qyh.blog.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.exception.SystemException;

public class FileUtils {

	private static final char SPLITER = '/';
	private static final char SPLITER2 = '\\';

	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>',
			'|', '\"', ':', '\u0000' };

	/**
	 * ${user.home}
	 */
	public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));

	/**
	 * 博客用来存放临时文件的文件夹
	 */
	public static final Path TEMP_DIR = HOME_DIR.resolve("blog_temp");

	private static final Predicate<Path> TRUE = path -> true;

	static {
		forceMkdir(TEMP_DIR);
	}

	private FileUtils() {

	}

	/**
	 * 采用UUID创造一个文件，这个文件为系统临时文件，会通过定时任务删除
	 * 
	 * @param ext
	 *            文件后缀
	 * @return 临时文件
	 * @see FileUtils#clearAppTemp(Predicate)
	 */
	public static Path appTemp(String ext) {
		String name = StringUtils.uuid() + "." + ext;
		try {
			return Files.createFile(TEMP_DIR.resolve(name));
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	/**
	 * 获取文件的后缀名
	 * 
	 * @param fullName
	 * @return
	 */
	public static String getFileExtension(String fullName) {
		String fileName = new File(fullName).getName();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}

	/**
	 * 获取文件的后缀名
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileExtension(Path path) {
		String fileName = path.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}

	/**
	 * 获取文件名(不包括后缀)
	 * 
	 * @param file
	 * @return
	 */
	public static String getNameWithoutExtension(String file) {
		String fileName = new File(file).getName();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
	}

	/**
	 * 删除文件|文件夹
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteQuietly(Path path) {
		return deleteQuietly(path, TRUE);
	}

	/**
	 * 删除文件|文件夹
	 * 
	 * @param path
	 *            路径
	 * @param filter
	 *            过滤条件
	 */
	public static boolean deleteQuietly(Path path, final Predicate<Path> filter) {
		Objects.requireNonNull(filter);
		if (!exists(path)) {
			return true;
		}
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (filter.test(file)) {
						deleteOneFileQuietly(file);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (filter.test(dir)) {
						deleteOneFileQuietly(dir);
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void deleteOneFileQuietly(Path file) {
		try {
			Files.delete(file);
		} catch (IOException e) {

		}
	}

	/**
	 * 创建一个文件
	 * 
	 * @param path
	 */
	public static void createFile(Path path) {
		Objects.requireNonNull(path);
		synchronized (FileUtils.class) {
			if (!exists(path)) {
				try {
					Files.createDirectories(path.getParent());
					Files.createFile(path);
				} catch (IOException e) {
					throw new SystemException("创建文件夹：" + path + "失败:" + e.getMessage(), e);
				}
			} else {
				if (!isRegularFile(path)) {
					throw new SystemException("目标位置" + path + "已经存在文件，但不是文件");
				}
			}
		}
	}

	/**
	 * 判读路径是否指向一个文件
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isRegularFile(Path path) {
		return path != null && Files.isRegularFile(path);
	}

	/**
	 * 创建一个文件夹，如果失败，抛出异常
	 * 
	 * @param path
	 */
	public static void forceMkdir(Path path) {
		if (path == null) {
			return;
		}
		if (exists(path) && isDirectory(path)) {
			return;
		}
		synchronized (FileUtils.class) {
			if (!exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					throw new SystemException("创建文件夹：" + path + "失败:" + e.getMessage(), e);
				}
			} else {
				if (!isDirectory(path)) {
					throw new SystemException("目标位置" + path + "已经存在文件，但不是文件夹");
				}
			}
		}
	}

	/**
	 * 判断路径是否指向一个文件夹
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isDirectory(Path path) {
		return path != null && Files.isDirectory(path);
	}

	/**
	 * 拷贝一个文件
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copy(Path source, Path target) throws IOException {
		forceMkdir(target.getParent());
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * 移动一个文件
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void move(Path source, Path target) throws IOException {
		forceMkdir(target.getParent());
		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * 获取子文件
	 * <p>
	 * eg:sub(Paths.get("h:/123"), "/123/414\\wqeqw") ==>
	 * h:\123\123\414\wqeqw(windows)
	 * </p>
	 * 
	 * @param p
	 * @param sub
	 * @return
	 */
	public static Path sub(Path p, String sub) {
		Objects.requireNonNull(p);
		Objects.requireNonNull(sub);
		return p.resolve(cleanPath(sub));
	}

	/**
	 * 删除系统临时文件夹内符合条件的文件
	 * 
	 * @param predicate
	 */
	public static void clearAppTemp(Predicate<Path> predicate) {
		deleteQuietly(TEMP_DIR, path ->
		// 不删除文件夹
		!isDirectory(path) && predicate.test(path));
	}

	/**
	 * 删除连续的 '/'，开头和结尾的'/'
	 * 
	 * <p>
	 * cleanPath("\\\\////123/\\\\////456//\\\\////789.txt") =
	 * '123/456/789.txt';<br>
	 * cleanPath("") = ""; <br>
	 * cleanPath(" ") = " "; <br>
	 * cleanPath(null) = "";
	 * </p>
	 * 
	 * @param path
	 * @return
	 */
	public static String cleanPath(String path) {
		if (Validators.isEmptyOrNull(path, false)) {
			return "";
		}
		char[] chars = path.toCharArray();
		char prev = chars[0];
		char last = SPLITER;
		if (chars.length == 1) {
			if (prev == SPLITER || prev == SPLITER2) {
				return "";
			}
			return Character.toString(prev);
		}
		if (prev == SPLITER2) {
			prev = SPLITER;
		}
		StringBuilder sb = new StringBuilder();
		if (prev != SPLITER) {
			sb.append(prev);
		}
		for (int i = 1; i < chars.length; i++) {
			char ch = chars[i];
			if (ch == SPLITER || ch == SPLITER2) {
				if (prev == SPLITER) {
					continue;
				}
				prev = SPLITER;
				if (i < chars.length - 1) {
					sb.append(SPLITER);
					last = SPLITER;
				}
			} else {
				prev = ch;
				sb.append(ch);
				last = ch;
			}
		}
		if (last == SPLITER) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 判断文件是否是某个文件的子文件
	 * 
	 * @param dest
	 * @param parent
	 * @return 如果是子文件或者两者相同
	 */
	public static boolean isSub(Path dest, Path parent) {
		return dest.equals(parent) || dest.startsWith(parent.normalize());
	}

	/**
	 * Path to string
	 * 
	 * @param path
	 * @return
	 */
	public static String toString(Path path) throws IOException {
		return Files.readString(path, Constants.CHARSET);
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean exists(Path path) {
		return path != null && Files.exists(path);
	}

	/**
	 * 获取文件大小
	 * 
	 * @param path
	 * @return
	 */
	public static long getSize(Path path) {
		Objects.requireNonNull(path);
		try {
			return Files.size(path);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	/**
	 * 获取文件的最后修改时间
	 * 
	 * @param path
	 * @return 如果获取失败，返回-1
	 */
	public static long getLastModifiedTime(Path path) {
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * 提供一个简单的文件名校验，最終是否允许以能否成功创建文件为准
	 * <p>
	 * <b>不会对文件系统的保留文件名做校验，例如windows下的CON文件</b>
	 * </p>
	 * 
	 * @param name
	 *            包含后缀名的文件名
	 * @return
	 */
	public static boolean maybeValidateFilename(String name) {
		if (Validators.isEmptyOrNull(name, true)) {
			return false;
		}

		try {
			Paths.get(name);
		} catch (InvalidPathException e) {
			return false;
		}

		if (name.chars().anyMatch(FileUtils::isIllegalChar)) {
			return false;
		}

		if (name.endsWith(".")) {
			return false;
		}

		// 一些文件系統的保留名称不做校验 例如windows下的CON
		return !name.endsWith(" ");
	}

	private static boolean isIllegalChar(int ch) {
		for (char illegal : ILLEGAL_CHARACTERS) {
			if (illegal == ch) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 将字节转化为可读的文件大小
	 * </p>
	 * 
	 * {@link https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java}
	 * 
	 * @param bytes
	 * @param si
	 *            如果为true，那么以1000为一个单位，否则以1024为一个单位
	 * @return
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static Stream<Path> quietlyWalk(Path dir) {
		return quietlyWalk(dir, Integer.MAX_VALUE);
	}

	public static Stream<Path> quietlyWalk(Path dir, int maxDepth) {
		try {
			return Files.walk(dir, maxDepth);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}
}
