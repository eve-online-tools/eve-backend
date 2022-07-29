package de.ronnywalter.eve.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()

        ;
    }
}

/*
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .mvcMatchers("/", "/login/**").permitAll()
                .mvcMatchers("/secure").authenticated()
                .anyRequest().permitAll()
                .and()
                .oauth2Client()
                .and()
                .oauth2Login()
                .redirectionEndpoint()
                .baseUri("/auth/login/oauth2/code/eve")
                .and()
                .authorizationEndpoint()
                .baseUri("/auth/oauth2/authorization")
                .and()
                .defaultSuccessUrl("/auth/loginSuccess")
                .failureUrl("/auth/loginError")
                ;
        int a = 0;
    }

 */