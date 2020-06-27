package me.qyh.blog.web.template;

import me.qyh.blog.service.TemplateService;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class TemplateHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return TemplateService.TEMPLATE_NAME.equals(handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Map<String, Object> pathVars = (Map<String, Object>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return new ModelAndView(handler.toString()).addAllObjects(pathVars);
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }
}
