package com.vc.door.test1;

import com.vc.door.client.EnableSSO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("com.vc.door.test1.controller")
@EnableSSO
public class DoorTest1Application {

    public static void main(String[] args) {
        SpringApplication.run(DoorTest1Application.class, args);
    }
}
