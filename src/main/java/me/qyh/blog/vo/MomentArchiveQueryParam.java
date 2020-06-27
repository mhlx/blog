package me.qyh.blog.vo;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDate;

public class MomentArchiveQueryParam extends PageQueryParam {

    private boolean queryPrivate;
    private boolean asc;
    @Size(max = 20, message = "动态查询内容不能超过20个字符")
    private String query;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
    private boolean queryPasswordProtected;// whether query content that is password protected

    public boolean isQueryPrivate() {
        return queryPrivate;
    }

    public void setQueryPrivate(boolean queryPrivate) {
        this.queryPrivate = queryPrivate;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public boolean isQueryPasswordProtected() {
        return queryPasswordProtected;
    }

    public void setQueryPasswordProtected(boolean queryPasswordProtected) {
        this.queryPasswordProtected = queryPasswordProtected;
    }

}
