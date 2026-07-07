package org.example.y9_gaming_site.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/profile").setViewName("profile");
        registry.addViewController("/home").setViewName("homePage");
        registry.addViewController("/quizzes").setViewName("quizzes");
        registry.addViewController("/leaderboard").setViewName("leaderboard");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatars/**").addResourceLocations("file:uploads/avatars/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

    }
} 