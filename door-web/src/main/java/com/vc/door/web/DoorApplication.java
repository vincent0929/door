package com.vc.door.web;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan("com.vc.door")
@MapperScan("com.vc.door.core.dao")
@ServletComponentScan("com.vc.door.com.vc.door.client.web")
@EnableSwagger2
@EnableCaching
public class DoorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoorApplication.class, args);
    }
}
