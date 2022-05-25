package com.velokofi.events.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
                                "/assets/**",
                                "/img/**",
                                "/js/**",
                                "/vendor/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login();
    }

}