package me.qyh.blog.file;

import javax.validation.constraints.Size;

public class FileUpdate {
    private String content;
    @Path(message = "非法的文件夹路径")
    private String dirPath;
    @Size(max = 255, message = "文件名称不能超过255个字符")
    private String name;
    @Size(max = 20, message = "密码不能超过20个字符")
    private String password;
    private boolean isPrivate;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

}