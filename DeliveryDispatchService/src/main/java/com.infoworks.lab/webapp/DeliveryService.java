package com.infoworks.lab.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.infoworks.lab.controllers"
        , "com.infoworks.lab.webapp.config"
        , "com.infoworks.lab.domain"})
public class DeliveryService {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryService.class, args);
    }

}
