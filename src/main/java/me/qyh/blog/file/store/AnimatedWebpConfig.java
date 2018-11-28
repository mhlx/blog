package me.qyh.blog.file.store;

/**
 * https://developers.google.com/speed/webp/docs/gif2webp
 * 
 * @author wwwqyhme
 *
 */
public class AnimatedWebpConfig {

	private int sec;// 最长处理时间

	public enum Metadata {
		ALL("all"), NONE("none"), ICC("icc"), XMP("xmp");

		private final String value;

		Metadata(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

	public AnimatedWebpConfig() {
		super();
	}

	public AnimatedWebpConfig(AnimatedWebpConfig config) {
		this.lossy = config.lossy;
		this.metadata = config.metadata;
		this.method = config.method;
		this.mixed = config.mixed;
		this.q = config.q;
		this.sec = config.sec;
	}

	private boolean lossy = true;
	private boolean mixed = false;
	private float q = 75F;// 0~100

	private int method = 4;// 0~6

	private Metadata metadata = Metadata.NONE;

	public boolean isLossy() {
		return lossy;
	}

	public void setLossy(boolean lossy) {
		this.lossy = lossy;
	}

	public boolean isMixed() {
		return mixed;
	}

	public void setMixed(boolean mixed) {
		this.mixed = mixed;
	}

	public float getQ() {
		return q;
	}

	public void setQ(float q) {
		this.q = q;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}

}
