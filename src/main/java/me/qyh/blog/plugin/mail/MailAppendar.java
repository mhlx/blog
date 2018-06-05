/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.plugin.mail;

import org.slf4j.Marker;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnErrorEvaluator;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.sift.DefaultDiscriminator;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.spi.CyclicBufferTracker;
import ch.qos.logback.core.util.ContentTypeUtil;
import me.qyh.blog.core.exception.SystemException;

/**
 * 这个用来将错误信息发送至管理员邮箱，由于借用了MailSender来发送邮件，所以append()方法必须在Spring容器初始化完成之后被调用
 * 
 * @see MailSender
 * @see SMTPAppender
 * @author Administrator
 *
 */
public class MailAppendar extends AppenderBase<ILoggingEvent> {

	// ~ 14 days
	static final long MAX_DELAY_BETWEEN_STATUS_MESSAGES = 1228800 * CoreConstants.MILLIS_IN_ONE_SECOND;

	long lastTrackerStatusPrint = 0;
	long delayBetweenStatusMessages = 300 * CoreConstants.MILLIS_IN_ONE_SECOND;

	private final MailSender mailSender;

	private Layout<ILoggingEvent> subjectLayout;
	private Layout<ILoggingEvent> layout;
	private EventEvaluator<ILoggingEvent> eventEvaluator;

	private Discriminator<ILoggingEvent> discriminator = new DefaultDiscriminator<>();
	private CyclicBufferTracker<ILoggingEvent> cbTracker;
	private int errorCount;

	private boolean includeCallerData = false;
	private final String subjectPattern;

	public MailAppendar(MailSender mailSender, String subjectPattern) {
		super();
		this.mailSender = mailSender;
		this.subjectPattern = subjectPattern;
	}

	/**
	 * 这个方法的调用必须在spring容器初始化之后
	 */
	@Override
	protected void append(ILoggingEvent eventObject) {
		if (!checkEntryConditions()) {
			return;
		}

		String key = discriminator.getDiscriminatingValue(eventObject);
		long now = System.currentTimeMillis();
		final CyclicBuffer<ILoggingEvent> cb = cbTracker.getOrCreate(key, now);

		if (includeCallerData) {
			eventObject.getCallerData();
		}
		eventObject.prepareForDeferredProcessing();
		cb.add(eventObject);

		try {
			if (eventEvaluator.evaluate(eventObject)) {
				// clone the CyclicBuffer before sending out asynchronously
				CyclicBuffer<ILoggingEvent> cbClone = new CyclicBuffer<>(cb);
				// see http://jira.qos.ch/browse/LBCLASSIC-221
				cb.clear();
				// build MessageBean
				sendMail(cbClone, eventObject);

			}
		} catch (EvaluationException ex) {
			errorCount++;
			if (errorCount < CoreConstants.MAX_ERROR_COUNT) {
				addError("SMTPAppender's EventEvaluator threw an Exception-", ex);
			}
		}

		// immediately remove the buffer if asked by the user
		if (eventMarksEndOfLife(eventObject)) {
			cbTracker.endOfLife(key);
		}

		cbTracker.removeStaleComponents(now);

		if (lastTrackerStatusPrint + delayBetweenStatusMessages < now) {
			addInfo("MailAppender [" + name + "] is tracking [" + cbTracker.getComponentCount() + "] buffers");
			lastTrackerStatusPrint = now;
			// quadruple 'delay' assuming less than max delay
			if (delayBetweenStatusMessages < MAX_DELAY_BETWEEN_STATUS_MESSAGES) {
				delayBetweenStatusMessages *= 4;
			}
		}
	}

	private void sendMail(CyclicBuffer<ILoggingEvent> cb, ILoggingEvent lastEventObject) {
		try {
			StringBuilder sbuf = new StringBuilder();
			String header = layout.getFileHeader();
			if (header != null) {
				sbuf.append(header);
			}
			String presentationHeader = layout.getPresentationHeader();
			if (presentationHeader != null) {
				sbuf.append(presentationHeader);
			}

			int len = cb.length();
			for (int i = 0; i < len; i++) {
				ILoggingEvent event = cb.get();
				sbuf.append(layout.doLayout(event));
			}

			String presentationFooter = layout.getPresentationFooter();
			if (presentationFooter != null) {
				sbuf.append(presentationFooter);
			}
			String footer = layout.getFileFooter();
			if (footer != null) {
				sbuf.append(footer);
			}

			String subjectStr = "Undefined subject";
			if (subjectLayout != null) {
				subjectStr = subjectLayout.doLayout(lastEventObject);
				if (subjectStr == null) {
					throw new SystemException("邮件发送标题不能为null");
				}
				// The subject must not contain new-line characters, which cause
				// an SMTP error (LOGBACK-865). Truncate the string at the first
				// new-line character.
				int newLinePos = subjectStr.indexOf('\n');
				if (newLinePos > -1) {
					subjectStr = subjectStr.substring(0, newLinePos);
				}
			}
			String contentType = layout.getContentType();
			MessageBean mb = new MessageBean(subjectStr, !ContentTypeUtil.isTextual(contentType), sbuf.toString());
			mailSender.sendAsync(mb);
		} catch (Exception e) {
			addError("Error occurred while sending e-mail notification.", e);
		}
	}

	@Override
	public void start() {
		if (cbTracker == null) {
			cbTracker = new CyclicBufferTracker<>();
		}

		if (this.eventEvaluator == null) {
			OnErrorEvaluator onError = new OnErrorEvaluator();
			onError.setContext(getContext());
			onError.setName("onError");
			onError.start();
			this.eventEvaluator = onError;
		}

		PatternLayout pl = new PatternLayout();
		pl.setContext(getContext());
		pl.setPattern(subjectPattern);
		// we don't want a ThrowableInformationConverter appended
		// to the end of the converter chain
		// This fixes issue LBCLASSIC-67
		pl.setPostCompileProcessor(null);
		pl.start();
		subjectLayout = pl;

		super.start();
	}

	@Override
	public synchronized void stop() {
		this.started = false;
	}

	private boolean eventMarksEndOfLife(ILoggingEvent eventObject) {
		Marker marker = eventObject.getMarker();
		if (marker == null) {
			return false;
		}

		return marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER);
	}

	private boolean checkEntryConditions() {
		if (!this.started) {
			addError("Attempting to append to a non-started appender: " + this.getName());
			return false;
		}
		if (this.eventEvaluator == null) {
			addError("No EventEvaluator is set for appender [" + name + "].");
			return false;
		}
		if (this.layout == null) {
			addError("No layout set for appender named [" + name
					+ "]. For more information, please visit http://logback.qos.ch/codes.html#smtp_no_layout");
			return false;
		}
		if (mailSender == null) {
			addError("mail Sender is null");
			return false;
		}
		return true;
	}

	public void setLayout(Layout<ILoggingEvent> layout) {
		this.layout = layout;
	}

	public void setEvaluator(EventEvaluator<ILoggingEvent> eventEvaluator) {
		this.eventEvaluator = eventEvaluator;
	}

	public void setIncludeCallerData(boolean includeCallerData) {
		this.includeCallerData = includeCallerData;
	}
}
