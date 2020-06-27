package me.qyh.blog.file;

import java.util.Map;

public class FileInfoDetail extends FileInfo {

    public FileInfoDetail(FileInfo fi) {
        super(fi);
    }

    private String content;// 文件内容，只有当文件可编辑的时候才会返回
    private Map<String, Object> properties;// 文件属性

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
