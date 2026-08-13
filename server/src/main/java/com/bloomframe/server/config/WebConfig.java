package com.bloomframe.server.config;

import com.bloomframe.server.common.security.AuthenticatedUidArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedUidArgumentResolver authenticatedUidArgumentResolver;

    public WebConfig(AuthenticatedUidArgumentResolver authenticatedUidArgumentResolver) {
        this.authenticatedUidArgumentResolver = authenticatedUidArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUidArgumentResolver);
    }
}