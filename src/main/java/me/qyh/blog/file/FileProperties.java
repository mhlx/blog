package me.qyh.blog.file;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "blog.file")
@Validated
@Conditional(FileCondition.class)
public class FileProperties {

	@NotBlank
	private String rootPath;
	private String thumbPath;

	private String graphicsMagickPath;
	private String pngQuantPath;
	private String gifsiclePath;
	private String ffmpegPath;

	private boolean sourceProtect;// 原图保护

	@Min(1)
	private int totalSem = 5;
	// 每次处理图片文件时，获取信号数
	@Min(1)
	private int imageSemPer = 1;
	// 每次处理视频文件时，获取信号数
	@Min(1)
	private int videoSemPer = 5;
	private int[] imageSizes = new int[] { 64, 200, 400, 600, 900, 960, 1920 };
	@Min(50)
	private int videoSize = 960;
	// 缩略图尺寸
	@Min(16)
	private int smallThumbSize = 200;
	@Min(16)
	private int middleThumbSize = 600;
	@Min(16)
	private int largeThumbSize = 900;

	public String getPngQuantPath() {
		return pngQuantPath;
	}

	public void setPngQuantPath(String pngQuantPath) {
		this.pngQuantPath = pngQuantPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	public String getGraphicsMagickPath() {
		return graphicsMagickPath;
	}

	public void setGraphicsMagickPath(String graphicsMagickPath) {
		this.graphicsMagickPath = graphicsMagickPath;
	}

	public String getGifsiclePath() {
		return gifsiclePath;
	}

	public void setGifsiclePath(String gifsiclePath) {
		this.gifsiclePath = gifsiclePath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getThumbPath() {
		return thumbPath;
	}

	public void setThumbPath(String thumbPath) {
		this.thumbPath = thumbPath;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public int getTotalSem() {
		return totalSem;
	}

	public void setTotalSem(int totalSem) {
		this.totalSem = totalSem;
	}

	public int getImageSemPer() {
		return imageSemPer;
	}

	public void setImageSemPer(int imageSemPer) {
		this.imageSemPer = imageSemPer;
	}

	public int getVideoSemPer() {
		return videoSemPer;
	}

	public void setVideoSemPer(int videoSemPer) {
		this.videoSemPer = videoSemPer;
	}

	public int[] getImageSizes() {
		return imageSizes;
	}

	public void setImageSizes(int[] imageSizes) {
		this.imageSizes = imageSizes;
	}

	public int getVideoSize() {
		return videoSize;
	}

	public void setVideoSize(int videoSize) {
		this.videoSize = videoSize;
	}

	public int getSmallThumbSize() {
		return smallThumbSize;
	}

	public void setSmallThumbSize(int smallThumbSize) {
		this.smallThumbSize = smallThumbSize;
	}

	public int getMiddleThumbSize() {
		return middleThumbSize;
	}

	public void setMiddleThumbSize(int middleThumbSize) {
		this.middleThumbSize = middleThumbSize;
	}

	public int getLargeThumbSize() {
		return largeThumbSize;
	}

	public void setLargeThumbSize(int largeThumbSize) {
		this.largeThumbSize = largeThumbSize;
	}

	public boolean isSourceProtect() {
		return sourceProtect;
	}

	public void setSourceProtect(boolean sourceProtect) {
		this.sourceProtect = sourceProtect;
	}

}
