package com.vc.door.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan("com.vc.door.client")
@ConditionalOnBean(DoorClientAutoConfiguration.DoorClientAutoConfigurationIndicator.class)
public class DoorClientAutoConfiguration implements WebMvcConfigurer {

    static class DoorClientAutoConfigurationIndicator {

    }
}
