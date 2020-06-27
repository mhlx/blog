package me.qyh.blog.security;

import me.qyh.blog.BlogContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class BlackIpFilter implements Filter {

    private final BlackIpService blackIpService;

    public BlackIpFilter(BlackIpService blackIpService) {
        super();
        this.blackIpService = blackIpService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Optional<String> opIp = BlogContext.getIP();
        if (opIp.isEmpty() || blackIpService.isBlackIp(opIp.get())) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

}
