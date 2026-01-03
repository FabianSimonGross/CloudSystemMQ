package de.nimzan.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MasterApp {
    public static void main(String[] args) {
        SpringApplication.run(MasterApp.class, args);
    }
}
