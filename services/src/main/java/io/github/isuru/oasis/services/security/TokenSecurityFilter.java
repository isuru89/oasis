package io.github.isuru.oasis.services.security;


import io.github.isuru.oasis.services.utils.AuthUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class TokenSecurityFilter implements Filter {

    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        String authHeader = req.getHeader(AUTHORIZATION);
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            if (authHeader.startsWith(BEARER)) {
                String token = authHeader.substring(BEARER.length());

                AuthUtils.TokenInfo tokenInfo = new AuthUtils.TokenInfo();
                tokenInfo.setUser(123);
                tokenInfo.setTeamId(1);
                req.setAttribute("token", tokenInfo);
                req.setAttribute("userId", tokenInfo.getUser()); // set user
                filterChain.doFilter(request, response);
            }
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "You are not allowed to access end point!");
        }
    }
}
