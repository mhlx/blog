package me.qyh.blog.file;

import java.util.Objects;

public class Resize {

    private Integer size;
    private Integer width;
    private Integer height;
    private boolean keepRatio;
    private Integer quality;// 0 ~ 100

    public Resize(Integer size) {
        super();
        this.size = size;
    }

    public Resize(Integer width, Integer height) {
        super();
        this.width = width;
        this.height = height;
    }

    public Resize(Integer width, Integer height, boolean keepRatio) {
        super();
        this.width = width;
        this.height = height;
        this.keepRatio = keepRatio;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public boolean isKeepRatio() {
        return keepRatio;
    }

    public void setKeepRatio(boolean keepRatio) {
        this.keepRatio = keepRatio;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    @Override
    public final String toString() {
        if (size != null) {
            return String.valueOf(size);
        }
        return Objects.toString(width, "_") + "x" + Objects.toString(height, "_") + (keepRatio ? "!" : "");
    }

    public boolean isInvalid() {
        if (size != null) {
            return size <= 0;
        } else {
            if (width == null && height == null)
                return true;
            if (width != null && width <= 0)
                return true;
            return height != null && height <= 0;
        }
    }

}
