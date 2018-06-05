package me.qyh.blog.plugin.sqlexport;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.zaxxer.hikari.HikariDataSource;

import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.file.store.ProcessUtils;

public class MySQLDump implements SqlDump {

	@Override
	public boolean matchProductName(String productName) {
		return "MySQL".equalsIgnoreCase(productName);
	}

	@Override
	public void doDump(HikariDataSource dataSource, Consumer<InputStream> consumer) throws Exception {
		try (Connection conn = dataSource.getConnection()) {
			Path temp = FileUtils.appTemp("sql");
			try {
				String[] cmdArray = new String[] { "mysqldump", "-u" + dataSource.getUsername(),
						"-p" + dataSource.getPassword(), conn.getCatalog(), "--result-file", temp.toString() };
				ProcessUtils.runProcess(cmdArray, 10, TimeUnit.SECONDS);
				try (InputStream is = Files.newInputStream(temp)) {
					consumer.accept(is);
				}
			} finally {
				FileUtils.deleteQuietly(temp);
			}
		}
	}

}
