package com.velokofi.events;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class VeloKofiEventsApplication {

    public static final Long LEADER_BOARD_LIMIT = 5L;

    public static final BigDecimal PLEDGE_DISTANCE = new BigDecimal(4044);

    public static final int ACTIVITIES_PER_PAGE = 200;

    public static final String START_TIMESTAMP; // 00:00:00 on 08 Jan 2022

    static {
        final OffsetDateTime after = OffsetDateTime.of(2022, 1, 8, 0, 0, 0, 0, ZoneOffset.of("+05:30"));
        START_TIMESTAMP = String.valueOf(after.toEpochSecond());
    }

    public static final String END_TIMESTAMP; // 23:59:59 on 18 Feb 2022

    static {
        final OffsetDateTime before = OffsetDateTime.of(2022, 2, 18, 23, 59, 59, 0, ZoneOffset.of("+05:30"));
        END_TIMESTAMP = String.valueOf(before.toEpochSecond());
    }

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
        SpringApplication.run(VeloKofiEventsApplication.class, args);
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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
