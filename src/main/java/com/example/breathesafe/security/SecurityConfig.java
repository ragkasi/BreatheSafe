package com.example.breathesafe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http
            // Disable CSRF for demonstration (adjust as needed for production)
            .csrf(csrf -> csrf.disable())
            // Use the lambda DSL for request authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/sms-webhook").permitAll() // permit SMS webhook endpoint
                .anyRequest().authenticated()
            )
            // Add your custom JWT filters, which now receive the AuthenticationManager
            .addFilter(new JwtAuthenticationFilter(authManager))
            .addFilter(new JwtAuthorizationFilter(authManager));
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        // In-memory authentication for demonstration purposes
        return new InMemoryUserDetailsManager(
            User.withUsername("test")
                .password("{noop}test123") // {noop} indicates no encoding, for demo only
                .roles("USER")
                .build()
        );
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
