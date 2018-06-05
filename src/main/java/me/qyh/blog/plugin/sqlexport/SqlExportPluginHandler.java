package me.qyh.blog.plugin.sqlexport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;

import com.zaxxer.hikari.HikariDataSource;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.plugin.Menu;
import me.qyh.blog.core.plugin.MenuRegistry;
import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.AttemptLoggerManager;
import me.qyh.blog.core.security.GoogleAuthenticator;

public class SqlExportPluginHandler extends PluginHandlerSupport {

	private List<SqlDump> dumps = new ArrayList<>();

	private HikariDataSource dataSource;
	private GoogleAuthenticator ga;
	private AttemptLoggerManager attemptLoggerManager;

	private static final String ENABLE_KEY = "plugin.sqlexport.enable";
	private static final String ATTEMPT_COUNT_KEY = "plugin.sqlexport.attemptCount";
	private static final String ATTEMPT_COUNT_SEC_KEY = "plugin.sqlexport.attemptSec";

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private boolean enable = pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);

	@Override
	public void init(ApplicationContext applicationContext) throws Exception {
		try {
			ga = applicationContext.getBean(GoogleAuthenticator.class);
		} catch (BeansException e) {
		}
		if (ga != null) {
			this.dataSource = applicationContext.getBean(HikariDataSource.class);
			this.attemptLoggerManager = applicationContext.getBean(AttemptLoggerManager.class);
			dumps.add(new MySQLDump());
			dumps.add(new H2SqlDump());
		}
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		if (ga != null) {
			int count = pluginProperties.get(ATTEMPT_COUNT_KEY).map(Integer::parseInt).orElse(5);
			int sec = pluginProperties.get(ATTEMPT_COUNT_SEC_KEY).map(Integer::parseInt).orElse(1800);
			AttemptLogger logger = attemptLoggerManager.createAttemptLogger(count, count, sec);

			registry.register(SqlDumpController.class.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(SqlDumpController.class)
							.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(dumps)
							.addConstructorArgValue(dataSource).addConstructorArgValue(ga)
							.addConstructorArgValue(logger).getBeanDefinition());
		}
	}

	@Override
	public void addMenu(MenuRegistry registry) throws Exception {
		if (ga != null) {
			registry.addMenu(new Menu(new Message("plugin.sqlexport.menu", "数据库导出"), "mgr/sqlExport"));
		}
	}

	@Override
	public boolean enable() {
		return enable;
	}

}
