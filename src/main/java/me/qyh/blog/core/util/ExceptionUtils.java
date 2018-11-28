package me.qyh.blog.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ExceptionUtils {

	private ExceptionUtils() {

	}

	/**
	 * 获取异常集合
	 * 
	 * @param throwable
	 * @return
	 */
	public static List<Throwable> getThrowableList(Throwable throwable) {
		List<Throwable> list = new ArrayList<>();
		Throwable e = throwable;
		while (e != null && !list.contains(e)) {
			list.add(e);
			e = e.getCause();
		}
		return list;
	}

	/**
	 * 从集合中取出指定的异常(包括子异常)
	 * 
	 * @param throwable
	 *            异常
	 * @param types
	 *            类型数组，
	 * @return
	 */
	public static Optional<Throwable> getFromChain(Throwable throwable, Class<?>... types) {
		if (throwable == null || types == null || types.length == 0) {
			return Optional.empty();
		}
		List<Throwable> throwables = getThrowableList(throwable);
		Collections.reverse(throwables);
		for (Throwable th : throwables) {
			if (matchType(th, types)) {
				return Optional.of(th);
			}
		}
		return Optional.empty();
	}

	/**
	 * 
	 * @param throwable
	 * @return
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter errors = new StringWriter();
		throwable.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	private static boolean matchType(Throwable th, Class<?>... types) {
		return th != null && Arrays.stream(types).anyMatch(type -> type.isAssignableFrom(th.getClass()));
	}
}
