package me.qyh.blog.plugin.syslock.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.plugin.syslock.entity.QALock;

@Component
public class QALockValidator extends SysLockValidator {

	private static final int MAX_QUESTION_LENGTH = 10000;
	private static final int MAX_ANSWERS_LENGTH = 500;
	private static final int MAX_ANSWERS_SIZE = 10;// 答案的个数

	@Override
	public boolean supports(Class<?> clazz) {
		return QALock.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		super.validate(target, errors);
		if (errors.hasErrors()) {
			return;
		}
		QALock lock = (QALock) target;

		String question = lock.getQuestion();
		if (Validators.isEmptyOrNull(question, true)) {
			errors.reject("lock.question.empty", "问题不能为空");
			return;
		}
		if (question.length() > MAX_QUESTION_LENGTH) {
			errors.reject("lock.question.toolong", "问题不能超过" + MAX_QUESTION_LENGTH + "个字符");
			return;
		}

		String answers = lock.getAnswers();
		if (answers == null || answers.isEmpty()) {
			errors.reject("lock.answers.empty", "答案不能为空");
			return;
		}
		if (answers.length() > MAX_ANSWERS_LENGTH) {
			errors.reject("lock.answers.toolong", "答案不能超过" + MAX_ANSWERS_LENGTH + "个字符");
			return;
		}
		String[] answerArray = answers.split(",");
		if (answerArray.length > MAX_ANSWERS_SIZE) {
			errors.reject("lock.answers.oversize", "答案不能超过" + MAX_ANSWERS_SIZE + "个");
		}
	}
}
