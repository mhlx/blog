package me.qyh.blog.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component("loggingErrorHandler")
public class LoggingErrorHandler implements ErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingErrorHandler.class);

	@Override
	public void handleError(Throwable t) {
		LOGGER.error(t.getMessage(), t);
	}

}
