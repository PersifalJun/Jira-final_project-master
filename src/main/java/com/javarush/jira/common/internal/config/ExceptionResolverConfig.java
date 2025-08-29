package com.javarush.jira.common.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

@Configuration
public class ExceptionResolverConfig {

    @Bean
    @Primary
    public HandlerExceptionResolver customHandlerExceptionResolver() {  // Измените имя метода
        return new ExceptionHandlerExceptionResolver();
    }
}