package com.example.deliverydemoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeliveryDemoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryDemoApiApplication.class, args);
    }

}
