package org.thymeleaf.standard.expression;

/**
 * <p>
 * 禁用thymeleaf 3.0.10限制模式
 * </p>
 * 
 * @since 7.0
 * @author wwwqyhme
 *
 */
public class StandardExpressionExecutionContext {
	public static final StandardExpressionExecutionContext NORMAL = new StandardExpressionExecutionContext(false, false,
			false);
	public static final StandardExpressionExecutionContext RESTRICTED = NORMAL;
	public static final StandardExpressionExecutionContext RESTRICTED_FORBID_UNSAFE_EXP_RESULTS = NORMAL;

	private static final StandardExpressionExecutionContext NORMAL_WITH_TYPE_CONVERSION = new StandardExpressionExecutionContext(
			false, false, true);
	private static final StandardExpressionExecutionContext RESTRICTED_WITH_TYPE_CONVERSION = NORMAL_WITH_TYPE_CONVERSION;
	private static final StandardExpressionExecutionContext RESTRICTED_FORBID_UNSAFE_EXP_RESULTS_WITH_TYPE_CONVERSION = NORMAL_WITH_TYPE_CONVERSION;

	private final boolean restrictVariableAccess;
	private final boolean forbidUnsafeExpressionResults;
	private final boolean performTypeConversion;

	private StandardExpressionExecutionContext(final boolean restrictVariableAccess,
			final boolean forbidUnsafeExpressionResults, final boolean performTypeConversion) {
		super();
		this.restrictVariableAccess = restrictVariableAccess;
		this.forbidUnsafeExpressionResults = forbidUnsafeExpressionResults;
		this.performTypeConversion = performTypeConversion;
	}

	public boolean getRestrictVariableAccess() {
		return this.restrictVariableAccess;
	}

	public boolean getForbidUnsafeExpressionResults() {
		return this.forbidUnsafeExpressionResults;
	}

	public boolean getPerformTypeConversion() {
		return this.performTypeConversion;
	}

	public StandardExpressionExecutionContext withoutTypeConversion() {
		if (!getPerformTypeConversion()) {
			return this;
		}
		if (this == NORMAL_WITH_TYPE_CONVERSION) {
			return NORMAL;
		}
		if (this == RESTRICTED_WITH_TYPE_CONVERSION) {
			return RESTRICTED;
		}
		if (this == RESTRICTED_FORBID_UNSAFE_EXP_RESULTS_WITH_TYPE_CONVERSION) {
			return RESTRICTED_FORBID_UNSAFE_EXP_RESULTS;
		}
		return new StandardExpressionExecutionContext(getRestrictVariableAccess(), getForbidUnsafeExpressionResults(),
				false);
	}

	public StandardExpressionExecutionContext withTypeConversion() {
		if (getPerformTypeConversion()) {
			return this;
		}
		if (this == NORMAL) {
			return NORMAL_WITH_TYPE_CONVERSION;
		}
		if (this == RESTRICTED) {
			return RESTRICTED_WITH_TYPE_CONVERSION;
		}
		if (this == RESTRICTED_FORBID_UNSAFE_EXP_RESULTS) {
			return RESTRICTED_FORBID_UNSAFE_EXP_RESULTS_WITH_TYPE_CONVERSION;
		}
		return new StandardExpressionExecutionContext(getRestrictVariableAccess(), getForbidUnsafeExpressionResults(),
				true);
	}
}
