package com.melly.vacationmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VacationManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationManagerApplication.class, args);
    }

}
