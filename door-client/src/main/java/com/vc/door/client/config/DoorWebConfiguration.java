package com.vc.door.client.config;

import com.vc.door.client.interceptor.SsoLoginInterceptor;
import com.vc.door.client.service.SsoLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(DoorConfiguration.class)
@ConditionalOnBean(SsoLoginService.class)
@Configuration
public class DoorWebConfiguration implements WebMvcConfigurer {

    @Autowired
    private SsoLoginInterceptor ssoLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ssoLoginInterceptor);
    }
}
