package me.qyh.blog.plugin.sqlexport;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zaxxer.hikari.HikariDataSource;

public interface SqlDump {

	boolean matchProductName(String productName);

	/**
	 * 
	 * @param dataSource
	 * @param consumer
	 *            <b>必须关闭流</b>
	 * 
	 *            <pre>
	 * try(InputStream is = ){consumer.accept(is)}
	 *            </pre>
	 * 
	 * @throws Exception
	 */
	void doDump(HikariDataSource dataSource, Consumer<InputStream> consumer) throws Exception;

}
