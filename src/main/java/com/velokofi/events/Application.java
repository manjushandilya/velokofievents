package com.velokofi.events;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
@EnableScheduling
public class Application {

    public static final String COOKIE_ID = "clientId";

    public static final Long LEADER_BOARD_LIMIT = 5L;

    public static final int ACTIVITIES_PER_PAGE = 200;

    public static final String START_TIMESTAMP = "1641580200"; // 00:00:00 on 08 Jan 2022
    public static final String END_TIMESTAMP = "1645208999";   // 23:59:59 on 18 Feb 2022
    public static final ObjectMapper MAPPER;
    public static final List<String> SUPPORTED_RIDE_TYPES;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static {
        SUPPORTED_RIDE_TYPES = new ArrayList<>();
        SUPPORTED_RIDE_TYPES.add("Ride");
        SUPPORTED_RIDE_TYPES.add("VirtualRide");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getName());
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

    public enum MetricType {DISTANCE, ELEVATION, AVG_SPEED}

}
