package me.qyh.blog.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.qyh.blog.BlogContext;
import me.qyh.blog.BlogProperties;
import me.qyh.blog.Constants;
import me.qyh.blog.security.TokenUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public final class BlogContextFilter implements Filter {

    private final BlogProperties blogProperties;
    private final ObjectMapper objectMapper;

    public BlogContextFilter(BlogProperties blogProperties, ObjectMapper objectMapper) {
        super();
        this.blogProperties = blogProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            setContext((HttpServletRequest) request, (HttpServletResponse) response);
            chain.doFilter(request, response);
        } finally {
            BlogContext.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private void setContext(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ipHeader = blogProperties.getIpHeader();
        String ip;
        if (ipHeader != null) {
            ip = request.getHeader(ipHeader);
        } else {
            ip = request.getRemoteAddr();
        }
        BlogContext.setIP(ip);

        String token = request.getHeader(blogProperties.getTokenHeader());

        if (token != null && TokenUtil.valid(token)) {
            BlogContext.setAuthenticated(true);
        }

        if (!BlogContext.isAuthenticated()) {
            // try to get from session
            HttpSession session = request.getSession(false);
            if (session != null && Boolean.TRUE.equals(session.getAttribute(Constants.AUTHENTICATED_SESSION_KEY))) {
                BlogContext.setAuthenticated(true);
            }
        }

        String password = request.getHeader(blogProperties.getPasswordHeader());
        if (password != null) {
            try {
                Map<String, String> map = objectMapper.readValue(password, Map.class);
                BlogContext.setPasswordMap(map);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                BlogContext.setPasswordMap((Map<String, String>) session.getAttribute(Constants.PASSWORD_SESSION_KEY));
            }
        }
    }
}