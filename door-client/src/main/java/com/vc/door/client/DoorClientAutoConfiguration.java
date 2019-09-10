package com.vc.door.client;

import com.vc.door.client.service.SsoLoginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
@ConditionalOnBean(SsoLoginService.class)
public class DoorClientAutoConfiguration {
}
