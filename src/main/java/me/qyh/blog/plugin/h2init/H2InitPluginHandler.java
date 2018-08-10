package me.qyh.blog.plugin.h2init;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;

import org.h2.tools.RunScript;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.WebApplicationContext;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.plugin.PluginHandler;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.util.FileUtils;

public class H2InitPluginHandler implements PluginHandler {

	private Path config = FileUtils.HOME_DIR.resolve("blog/app.properties");

	private PluginProperties pros = PluginProperties.getInstance();

	private static final String ENABLE_KEY = "plugin.h2init.enable";
	private static final String PORT_DETECT_KEY = "plugin.h2init.detectPort";

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) throws Exception {
		initH2();
		WebApplicationContext wac = (WebApplicationContext) applicationContext;
		ServletContext sc;
		if ((sc = wac.getServletContext()) != null) {
			initAppPros(sc);
		}
	}

	private void initAppPros(ServletContext sc) throws Exception {
		FileUtils.createFile(config);
		Properties pros = new Properties();
		try (InputStream is = Files.newInputStream(config)) {
			pros.load(is);
		}
		String contextPath = sc.getContextPath();
		pros.setProperty("app.contextPath", contextPath);

		if (this.pros.get(PORT_DETECT_KEY).map(Boolean::parseBoolean).orElse(true))
			try {
				MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
				Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
						Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
				String port = objectNames.iterator().next().getKeyProperty("port");
				pros.setProperty("app.port", port);
			} catch (Throwable e) {
			}

		try (OutputStream out = Files.newOutputStream(config)) {
			pros.store(out, "");
		}
	}

	private void initH2() throws Exception {
		Properties dbPros;
		try {
			dbPros = PropertiesLoaderUtils.loadProperties(new ClassPathResource("resources/mybatis/db.properties"));
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		}
		String jdbcUrl = dbPros.getProperty("jdbc.jdbcUrl");
		if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:")) {
			return;
		}
		String user = dbPros.getProperty("jdbc.user", "root");
		String password = dbPros.getProperty("jdbc.password", "root");

		RunScript.execute(jdbcUrl, user, password, "classpath:me/qyh/blog/plugin/h2init/blog.h2.sql",
				StandardCharsets.UTF_8, false);

		// get version
		try (InputStream is = H2InitPluginHandler.class.getResourceAsStream("version.properties")) {
			Properties pros = new Properties();
			pros.load(is);
			String version = pros.getProperty("version");
			Resource resource = new ClassPathResource("me/qyh/blog/plugin/h2init/" + version + ".sql");
			if (resource.exists()) {
				try {
					RunScript.execute(jdbcUrl, user, password,
							"classpath:me/qyh/blog/plugin/h2init/" + version + ".sql", StandardCharsets.UTF_8, false);
				} catch (Exception e) {
					throw new SystemException(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public boolean enable() {
		return pros.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);
	}

}
