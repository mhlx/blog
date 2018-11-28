package me.qyh.blog.file.store.local;

import org.springframework.web.multipart.MultipartFile;

/**
 * 非图片文件存储器
 * 
 * @author Administrator
 *
 */
public class CommonResourceStore extends LocalResourceRequestHandlerFileStore {

	public CommonResourceStore(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}

	public CommonResourceStore() {
		super("file");
	}

	@Override
	public final boolean canStore(MultipartFile multipartFile) {
		return true;
	}
}
