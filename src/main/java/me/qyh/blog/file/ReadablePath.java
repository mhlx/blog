package me.qyh.blog.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * 用于资源的读取
 * 
 * @author wwwqyhme
 *
 */
public interface ReadablePath {
	InputStream getInputStream() throws IOException;

	String fileName();

	long size();

	long lastModified();

	String getExtension();
}