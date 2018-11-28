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
