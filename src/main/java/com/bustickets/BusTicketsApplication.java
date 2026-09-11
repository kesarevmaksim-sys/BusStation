package com.bustickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTicketsApplication.class, args);
    }
}
