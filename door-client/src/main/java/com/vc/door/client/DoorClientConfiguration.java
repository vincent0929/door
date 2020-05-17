package com.vc.door.client;

import com.vc.door.client.interceptor.SsoLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan("com.vc.door.client")
@Configuration
public class DoorClientConfiguration implements WebMvcConfigurer {

    @Autowired
    private SsoLoginInterceptor ssoLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ssoLoginInterceptor).excludePathPatterns("/error");
    }
}
