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
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/users/guest").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/visualExternals/**").permitAll()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/games/**").authenticated()
                        .requestMatchers("/leaderboard/**", "/leaderboard.html").authenticated()
                        .requestMatchers("/achievements/**").authenticated()
                        .requestMatchers("/streak/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/home", "/stats/home").authenticated()
                        .requestMatchers("/sudoku", "/sudoku.html").authenticated()
                        .requestMatchers("/api/sudoku/**").authenticated()
                        .requestMatchers("/joker", "/joker/**").authenticated()
                        .requestMatchers("/api/joker/**").authenticated()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/quizzes", "/quizzes/**", "/quizzes.html", "/addQuiz.html", "/api/quizzes/new").authenticated()
                        .requestMatchers("/api/quizzes/**").authenticated()
                        .requestMatchers("/profile", "/profile/**", "/profile.html").authenticated()
                        .requestMatchers("/css/profile.css", "/js/profile.js").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/")
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/")
                        )
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}