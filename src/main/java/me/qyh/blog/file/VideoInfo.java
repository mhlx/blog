package me.qyh.blog.file;

public class VideoInfo {
    private final int width;
    private final int height;
    private final int duration;

    VideoInfo(int width, int height, int duration) {
        super();
        this.width = width;
        this.height = height;
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "VideoInfo [width=" + width + ", height=" + height + ", duration=" + duration + "]";
    }

}