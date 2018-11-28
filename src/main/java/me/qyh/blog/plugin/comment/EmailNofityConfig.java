package me.qyh.blog.plugin.comment;

public class EmailNofityConfig {

	private String templateLocation;
	private String mailSubject;
	private int messageTipCount;
	private int processSendSec;
	private int forceSendSec;

	EmailNofityConfig() {
		super();
	}

	public String getTemplateLocation() {
		return templateLocation;
	}

	public void setTemplateLocation(String templateLocation) {
		this.templateLocation = templateLocation;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public int getMessageTipCount() {
		return messageTipCount;
	}

	public void setMessageTipCount(int messageTipCount) {
		this.messageTipCount = messageTipCount;
	}

	public int getProcessSendSec() {
		return processSendSec;
	}

	public void setProcessSendSec(int processSendSec) {
		this.processSendSec = processSendSec;
	}

	public int getForceSendSec() {
		return forceSendSec;
	}

	public void setForceSendSec(int forceSendSec) {
		this.forceSendSec = forceSendSec;
	}

}
