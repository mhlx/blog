package me.qyh.blog.file;

public class ImageInfo {
	private final int width;
	private final int height;
	private final String type;// PNG|JPEG|GIF|WEBP

	ImageInfo(int width, int height, String type) {
		super();
		this.width = width;
		this.height = height;
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "ImageInfo [width=" + width + ", height=" + height + ", type=" + type + "]";
	}

}
