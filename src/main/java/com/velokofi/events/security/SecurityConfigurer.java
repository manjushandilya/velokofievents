package com.velokofi.events.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.client.RestTemplate;

@EnableWebSecurity
@Configuration
@Getter
@Setter
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**", "/cache/**").anyRequest();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // https://stackoverflow.com/questions/49273847/springboot-error-parsing-http-request-header
        http.headers().httpStrictTransportSecurity().disable();

        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login**", "/webjars/**", "/error**").permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}