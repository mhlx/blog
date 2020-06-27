package me.qyh.blog.web.template.tag;

public class RedirectException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String url;
    private final boolean permanently;

    RedirectException(String url, boolean permanently) {
        super(null, null, false, false);
        this.url = url;
        this.permanently = permanently;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPermanently() {
        return permanently;
    }

}
