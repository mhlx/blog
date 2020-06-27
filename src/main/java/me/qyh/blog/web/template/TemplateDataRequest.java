package me.qyh.blog.web.template;

import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.web.template.tag.DataTagProcessor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * this request used to lookup data by {@link DataTagProcessor}
 * <p>
 * it's reusable but <b>NOT THREAD SAFE</b>
 * </p>
 *
 * @author wwwqyhme
 * @see DataTagProcessor
 * @see ProcessContext
 */
public class TemplateDataRequest extends HttpServletRequestWrapper {

    private Map<String, String[]> parameterMap;
    private UriComponents components;
    private final Map<String, Object> attributes = new HashMap<>();
    private String path;

    public TemplateDataRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getMethod() {
        return RequestMethod.GET.name();
    }

    @Override
    public String getParameter(String name) {
        String[] value = parameterMap.get(name);
        if (value != null && value.length > 0) {
            return value[0];
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    @Override
    public String getRequestURI() {
        String contextPath = getContextPath();
        return contextPath + this.components.toUriString();
    }

    @Override
    public String getServletPath() {
        return this.components.getPath();
    }

    @Override
    public String getQueryString() {
        return this.components.getQuery();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public String getHeader(String name) {
        if ("x-requested-with".equals(name)) {
            return "XMLHttpRequest";
        }
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = getHeader(name);
        if (value == null) {
            return null;
        }
        return Collections.enumeration(List.of(value));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(Set.of("x-requested-with"));
    }

    public String getPath() {
        return path;
    }

    public void reset(Map<String, String[]> parameterMap, String path) {
        this.parameterMap = Collections.unmodifiableMap(parameterMap);
        this.path = '/' + FileUtils.cleanPath(path);
        this.components = UriComponentsBuilder.fromUriString(path).build();
        this.attributes.clear();
    }

}
