package org.example.y9_gaming_site.homePage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — auth guard
 *
 * Rules:
 *  - Static assets (CSS, JS, images) are always public.
 *  - /login, /register, /guest  are always public.
 *  - Everything else (including /stats/home) requires login.
 *
 * Guest accounts: when a user clicks "Play as Guest" you create
 * a temporary User with role GUEST and log them in normally via
 * UsernamePasswordAuthenticationToken — Spring Security treats
 * them as authenticated, so the guard lets them through.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // public assets
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        // public pages
                        .requestMatchers("/login", "/register", "/guest").permitAll()
                        // everything else needs a logged-in user (including guests)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home.html", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}
