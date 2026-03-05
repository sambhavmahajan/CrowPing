package com.github.sambhavmahajan.crowping.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(
                        csrf -> csrf.disable()
                ).authorizeHttpRequests(
                        authorize -> authorize.
                                requestMatchers("/home","/ping", "/verify/**").permitAll().
                                requestMatchers("/login", "/register").anonymous()
                                .anyRequest().authenticated()
                ).formLogin(
                        form -> form.loginPage("/login")
                                .defaultSuccessUrl("/dashboard", true).permitAll()
                ).logout(
                        logout -> logout.logoutSuccessUrl("/login?logout")
                )
                .rememberMe(Customizer.withDefaults()).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}