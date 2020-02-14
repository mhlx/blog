package me.qyh.blog.vo;

public class SavedComment {

	private final int id;
	private final boolean checking;

	public SavedComment(int id, boolean checking) {
		super();
		this.id = id;
		this.checking = checking;
	}

	public int getId() {
		return id;
	}

	public boolean isChecking() {
		return checking;
	}

}
