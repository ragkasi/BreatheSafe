package com.example.lockersystem.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // For example: permit all for demonstration, but lock it down in production
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/sms-webhook").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilter(new JwtAuthenticationFilter(authenticationManager()))
            .addFilter(new JwtAuthorizationFilter(authenticationManager()));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // In-memory auth for testing (not recommended in real production)
        auth.inMemoryAuthentication()
            .withUser("test").password("{noop}test123").roles("USER");
    }
}
