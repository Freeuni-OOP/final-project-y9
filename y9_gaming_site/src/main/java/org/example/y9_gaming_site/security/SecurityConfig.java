package org.example.y9_gaming_site.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/index.html").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/users/guest").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/visualExternals/**").permitAll()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/games/**").authenticated()
                        .requestMatchers("/leaderboard/**", "/leaderboard.html").authenticated()
                        .requestMatchers("/achievements/**").authenticated()
                        .requestMatchers("/streak/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/home", "/stats/home").authenticated()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/games/**").permitAll()
                        .requestMatchers("/leaderboard/**", "/leaderboard.html").permitAll()
                        .requestMatchers("/achievements/**").permitAll()
                        .requestMatchers("/streak/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/quizzes", "/quizzes/**", "/quizzes.html").permitAll()
                        .requestMatchers("/api/quizzes/**").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}