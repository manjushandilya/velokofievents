package com.velokofi.events.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
@Setter
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // https://stackoverflow.com/questions/49273847/springboot-error-parsing-http-request-header
        http.headers().httpStrictTransportSecurity().disable();

        http
                .authorizeRequests(a -> a
                        .antMatchers(
                                "/",
                                "/error",
                                "/hungryvelos",
                                "/pledge",
                                "/webjars/**",
                                "/css/**",
                                "/img/**",
                                "/js/**",
                                "/vendor/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}