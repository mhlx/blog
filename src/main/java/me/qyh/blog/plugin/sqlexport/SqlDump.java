package me.qyh.blog.plugin.sqlexport;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zaxxer.hikari.HikariDataSource;

public interface SqlDump {

	boolean matchProductName(String productName);

	void doDump(HikariDataSource dataSource, Consumer<InputStream> consumer) throws Exception;

}
