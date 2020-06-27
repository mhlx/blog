package me.qyh.blog.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import me.qyh.blog.EmptyStringToNullDeserializer;
import me.qyh.blog.utils.WebUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Template implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer id;
    @JsonDeserialize(using = EmptyStringToNullDeserializer.class)
    private String pattern;// nullable
    @JsonDeserialize(using = EmptyStringToNullDeserializer.class)
    private String name;// nullable
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    private Boolean enable;
    @JsonDeserialize(using = EmptyStringToNullDeserializer.class)
    private String description;
    private Boolean allowComment;

    public Template() {
        super();
    }

    public Template(String name, String pattern, String content) {
        super();
        this.name = name;
        this.pattern = pattern;
        this.content = content;
    }

    public Template(Template source) {
        this.id = source.id;
        this.pattern = source.pattern;
        this.name = source.name;
        this.content = source.content;
        this.createTime = source.createTime;
        this.modifyTime = source.modifyTime;
        this.enable = source.enable;
        this.description = source.description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAllowComment() {
        return allowComment;
    }

    public void setAllowComment(Boolean allowComment) {
        this.allowComment = allowComment;
    }

    public boolean isDefinitelyPattern() {
        return pattern != null && !WebUtils.isPattern(pattern);
    }
}
