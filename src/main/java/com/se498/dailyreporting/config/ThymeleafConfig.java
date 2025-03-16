package com.se498.dailyreporting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

/**
 * Configuration for Thymeleaf templates
 */
@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    /**
     * Add Layout Dialect for Thymeleaf templates
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /**
     * Add view controllers for simple mappings
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root to the UI home page
        registry.addViewController("/").setViewName("forward:/ui/reports");

        // Map UI root to the reports page
        registry.addViewController("/ui").setViewName("forward:/ui/reports");

        // Map Weather UI home page
        registry.addViewController("/ui/weather").setViewName("weather/index");
    }
}
