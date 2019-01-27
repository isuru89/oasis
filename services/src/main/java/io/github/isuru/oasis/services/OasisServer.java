package io.github.isuru.oasis.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OasisServer {

    public static void main(String[] args) {
        SpringApplication.run(OasisServer.class, args);
    }

}
