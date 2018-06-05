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
