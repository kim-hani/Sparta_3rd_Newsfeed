package com.example.sparta_3rd_newsfeed.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 회원가입 및 로그인 요청은 필터에서 제외
        String requestURI = httpRequest.getRequestURI();

        if (requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/webjars") ||
                requestURI.equals("/swagger-ui.html")) {
            chain.doFilter(request, response);
            return;
        }

        if (requestURI.startsWith("/members/signup") || requestURI.startsWith("/members/login")) {
            chain.doFilter(request, response);
            return;
        }


        // 세션에서 사용자 ID 가져오기
        Object user = httpRequest.getSession().getAttribute("member");

        if (user == null) {
            log.warn("Unauthorized access attempt to {}", requestURI);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }

        chain.doFilter(request, response);
    }
}
