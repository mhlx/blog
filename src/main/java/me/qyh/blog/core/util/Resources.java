package me.qyh.blog.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.config.Constants;

public final class Resources {

	private Resources() {
	}

	/**
	 * 读取Resource资源内容
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public static String readResourceToString(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		return read(is);
	}

	/**
	 * 读取resource内容
	 * 
	 * @param resource
	 * @param consumer
	 * @throws IOException
	 */
	public static void readResource(Resource resource, ResourceConsumer consumer) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			consumer.accept(is);
		}
	}

	/**
	 * 转化resource中的内容
	 * 
	 * @param resource
	 * @param fun
	 * @return
	 * @throws IOException
	 */
	public static <T> T applyResource(Resource resource, ResourceFunction<T> fun) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			return fun.apply(is);
		}
	}

	@FunctionalInterface
	public interface ResourceFunction<R> {
		R apply(InputStream is) throws IOException;
	}

	@FunctionalInterface
	public interface ResourceConsumer {
		void accept(InputStream is) throws IOException;
	}

	public static String read(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		try (InputStream _in = in;
				BufferedReader reader = new BufferedReader(new InputStreamReader(_in, Constants.CHARSET))) {
			return reader.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}
}
