package me.qyh.blog.web.listener;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import me.qyh.blog.core.config.UrlHelper;

public class AppContextLoaderListener extends ContextLoaderListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppContextLoaderListener.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);

		WebApplicationContext ctx = getCurrentWebApplicationContext();
		UrlHelper helper = ctx.getBean(UrlHelper.class);
		ServletContext sc = event.getServletContext();
		EnumSet<SessionTrackingMode> modes = EnumSet.of(SessionTrackingMode.COOKIE);
		sc.setSessionTrackingModes(modes);
		SessionCookieConfig config = sc.getSessionCookieConfig();
		config.setHttpOnly(true);
		config.setSecure(helper.isSecure());
		config.setPath("/" + helper.getContextPath());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					DriverManager.deregisterDriver(driver);
				} catch (SQLException e) {
					LOGGER.warn("注销driver失败：" + e.getMessage(), e);
				}
			}
		}

		shutdownMysqlIfAvailable();
	}

	private void shutdownMysqlIfAvailable() {
		try {
			Lookup lookup = MethodHandles.lookup();

			try {
				// mysql 8.x
				Class<?> clazz = lookup.findClass("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
				lookup.findStatic(clazz, "checkedShutdown", MethodType.methodType(void.class)).invoke();
			} catch (ClassNotFoundException e) {
				// mysql 5.x
				Class<?> clazz = lookup.findClass("com.mysql.jdbc.AbandonedConnectionCleanupThread");
				lookup.findStatic(clazz, "checkedShutdown", MethodType.methodType(void.class)).invoke();
			}
		} catch (Throwable e) {
		}
	}

}
