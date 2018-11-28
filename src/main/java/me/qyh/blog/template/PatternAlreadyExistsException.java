package me.qyh.blog.template;

public final class PatternAlreadyExistsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String pattern;
	private final String matchPattern;
	private final boolean keyPath;

	public PatternAlreadyExistsException(String pattern, String matchPattern) {
		super(null, null, false, false);
		this.pattern = pattern;
		this.matchPattern = matchPattern;
		this.keyPath = false;
	}

	public PatternAlreadyExistsException(String pattern) {
		super(null, null, false, false);
		this.pattern = pattern;
		this.matchPattern = null;
		this.keyPath = true;
	}

	public String getPattern() {
		return pattern;
	}

	public String getMatchPattern() {
		return matchPattern;
	}

	public boolean isKeyPath() {
		return keyPath;
	}
}
