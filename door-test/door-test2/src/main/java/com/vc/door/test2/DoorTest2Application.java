package com.vc.door.test2;

import com.vc.door.client.EnableSSO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("com.vc.door.test2.controller")
@EnableSSO
public class DoorTest2Application {

    public static void main(String[] args) {
        SpringApplication.run(DoorTest2Application.class, args);
    }
}
