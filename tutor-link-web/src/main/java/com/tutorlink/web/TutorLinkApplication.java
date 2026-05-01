package com.tutorlink.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tutorlink")
@MapperScan("com.tutorlink.dao.mapper")
@EnableScheduling
public class TutorLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorLinkApplication.class, args);
    }
}
