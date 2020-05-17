package com.vc.door.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan("com.vc.door")
@MapperScan("com.vc.door.core.dao")
@ServletComponentScan("com.vc.door.web")
@EnableSwagger2
@EnableCaching
public class DoorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoorApplication.class, args);
    }
}
