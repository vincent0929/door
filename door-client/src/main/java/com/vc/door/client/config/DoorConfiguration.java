package com.vc.door.client.config;

import com.vc.door.client.interceptor.SsoLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DoorConfiguration {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Bean
    public SsoLoginInterceptor loginInterceptor() {
        return (SsoLoginInterceptor) beanFactory.createBean(SsoLoginInterceptor.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                true);
    }
}
