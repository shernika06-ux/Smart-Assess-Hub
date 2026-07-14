package com.univ.smartassesshub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Force the browser to ALWAYS fetch the latest index.html and static files.
        // This solves aggressive browser caching issues during development.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/")
                .setCacheControl(CacheControl.noCache().noStore().mustRevalidate().cachePrivate().sMaxAge(0, TimeUnit.SECONDS));
    }
}
