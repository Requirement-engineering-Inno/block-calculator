package org.example.blockcalc.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login", "/error", "/css/**", "/js/**", "/img/**", "/icon.svg", "/favicon.ico").permitAll()
                        // Production area settings are for managers; everybody opens the list and the area page.
                        .requestMatchers("/projects/new", "/projects/*/settings", "/projects/*/currency").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/projects", "/projects/*").authenticated()
                        .requestMatchers("/projects/**", "/users/**", "/settings/**").hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(l -> l.logoutSuccessUrl("/login"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
