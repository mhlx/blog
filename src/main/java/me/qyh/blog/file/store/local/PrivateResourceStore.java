package me.qyh.blog.file.store.local;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;

import me.qyh.blog.core.context.Environment;

/**
 * 私人文件存储器
 * <p><b>不应该被nginx等服务器代理！！！</b></p>
 */
public class PrivateResourceStore extends CommonResourceStore{

	public PrivateResourceStore(String urlPatternPrefix) {
		super(urlPatternPrefix);
	}
	
	public PrivateResourceStore() {
		super("private");
	}

	@Override
	protected final Resource findResource(HttpServletRequest request) throws IOException {
		Environment.doAuthencation();
		return super.findResource(request);
	}

	@Override
	protected final boolean getRegisterMapping() {
		return true;
	}	


}
