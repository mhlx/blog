package me.qyh.blog.file;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

class FileResourceResolver implements ResourceResolver {

    private static final String WEBP_ACCEPT = "image/webp";

    private final FileService fileService;

    public FileResourceResolver(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations,
                                    ResourceResolverChain chain) {
        if (request == null) {
            return null;
        }
        String accept = request.getHeader("Accept");
        boolean supportWEBP = accept != null && accept.contains(WEBP_ACCEPT);
        return fileService.getProcessedFile(requestPath, supportWEBP).map(ReadablePathResource::new).orElse(null);
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return null;
    }
}
