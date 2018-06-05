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
package me.qyh.blog.plugin.syslock.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;


/**
 * 问答锁
 * 
 * @author Administrator
 *
 */
public class QALock extends SysLock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String ANSWER_PARAMETER = "answers";

	private String question;
	private String answers;

	/**
	 * default
	 */
	public QALock() {
		super(SysLockType.QA);
	}

	@Override
	public LockKey getKeyFromRequest(HttpServletRequest request) throws LogicException {
		final String answer = request.getParameter(ANSWER_PARAMETER);
		if (answer == null || answer.isEmpty()) {
			throw new LogicException(new Message("lock.qa.answer.blank", "请填写问题答案"));
		}
		return new LockKey() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String lockId() {
				return getId();
			}

			@Override
			public Serializable getKey() {
				return answer;
			}
		};
	}

	@Override
	public void tryOpen(LockKey key) throws LogicException {
		Objects.requireNonNull(key);
		Object data = key.getKey();
		String answer = data.toString();
		if (isCorrectAnswer(answer)) {
			return;
		}
		throw new LogicException(new Message("lock.qa.unlock.fail", "答案错误"));
	}

	private boolean isCorrectAnswer(String answer) {
		Objects.requireNonNull(answers);
		return Arrays.stream(answers.split(",")).anyMatch(answer::equals);
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswers() {
		return answers;
	}

	public void setAnswers(String answers) {
		this.answers = answers;
	}
}
