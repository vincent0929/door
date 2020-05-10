package com.vc.door.test;

import com.vc.door.client.EnableSSO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.vc.door")
@ServletComponentScan("com.vc.door.test.controller")
@EnableSSO
public class DoorTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoorTestApplication.class, args);
    }
}
