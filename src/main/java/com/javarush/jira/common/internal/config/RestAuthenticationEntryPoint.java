package com.javarush.jira.common.internal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RequestMappingHandlerMapping mapping;

    public RestAuthenticationEntryPoint(
            HandlerExceptionResolver handlerExceptionResolver,
            RequestMappingHandlerMapping mapping) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.mapping = mapping;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws ServletException {
        try {
            var handler = mapping.getHandler(request);
            handlerExceptionResolver.resolveException(request, response,
                    handler == null ? null : handler.getHandler(), authException);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}