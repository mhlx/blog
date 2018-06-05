package me.qyh.blog.plugin.sqlexport;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.h2.tools.Script;

import com.zaxxer.hikari.HikariDataSource;

import me.qyh.blog.core.util.FileUtils;

public class H2SqlDump implements SqlDump {

	@Override
	public boolean matchProductName(String productName) {
		return "H2".equalsIgnoreCase(productName);
	}

	@Override
	public void doDump(HikariDataSource dataSource, Consumer<InputStream> consumer) throws Exception {
		/**
		 * @see http://h2database.com/javadoc/org/h2/tools/Script.html#process_String_String_String_String_String_String
		 */
		Path temp = FileUtils.appTemp("sql");
		try {
			Script.process(dataSource.getJdbcUrl(), dataSource.getUsername(), dataSource.getPassword(), temp.toString(),
					"", "");
			try (InputStream is = Files.newInputStream(temp)) {
				consumer.accept(is);
			}
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

}
