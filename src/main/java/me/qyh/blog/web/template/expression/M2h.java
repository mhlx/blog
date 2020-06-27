package me.qyh.blog.web.template.expression;

import me.qyh.blog.Markdown2Html;

final class M2h {

    private final Markdown2Html markdown2Html;

    public M2h(Markdown2Html markdown2Html) {
        super();
        this.markdown2Html = markdown2Html;
    }

    public String toHtml(String markdown) {
        return markdown2Html.toHtml(markdown);
    }

}
