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

    //public static final String START_TIMESTAMP = "1641580200";// 00:00:00 on 8 Jan 2022
    //public static final String END_TIMESTAMP = "1644604199";// 23:59:59 on 11 Feb 2022

    public static final String START_TIMESTAMP = "1638297000";// 00:00:00 on Dec 2021
    public static final String END_TIMESTAMP = "1640975399";// 23:59:59 on 31 Dec 2021

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
