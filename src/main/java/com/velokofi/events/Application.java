package com.velokofi.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static final Long LEADER_BOARD_LIMIT = 5L;

    public static final int ACTIVITIES_PER_PAGE = 200;

    public static final String START_TIMESTAMP = "1641580200"; // 00:00:00 on 08 Jan 2022
    public static final String END_TIMESTAMP = "1645208999";   // 23:59:59 on 18 Feb 2022

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }

}
