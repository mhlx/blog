package me.qyh.blog.file;

import java.time.Duration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

class FileResourceHttpRequestHandler extends ResourceHttpRequestHandler {

	public FileResourceHttpRequestHandler(FileResourceResolver fileResourceResolver,
			ResourceProperties resourceProperties) {
		super();
		this.setResourceResolvers(List.of(fileResourceResolver));
		Duration cachePeriod = resourceProperties.getCache().getPeriod();
		if (cachePeriod != null) {
			this.setCacheSeconds((int) cachePeriod.getSeconds());
		}
		CacheControl cacheControl = resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
		this.setCacheControl(cacheControl);
	}

	@Override
	protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
		MediaType type;
		if (resource instanceof ReadablePathSource) {
			type = ((ReadablePathSource) resource).getMediaType().orElse(null);
		} else {
			type = super.getMediaType(request, resource);
		}
		return type;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.getLocations().add(null);// we add a null resource to locations,just to ignore the warning,this will
										// never be used
		super.afterPropertiesSet();
	}

}
