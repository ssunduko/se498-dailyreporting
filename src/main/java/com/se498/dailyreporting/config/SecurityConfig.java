package com.se498.dailyreporting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((authz) -> {
                            try {
                                authz
                                        .requestMatchers("/css/**", "/js/**", "/signup", "/logout").permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/graphiql/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/voyager/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/altair/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/playground/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/soap/**")).permitAll()
                                        // Allow access to the GraphQL endpoint for the tools to work
                                        .requestMatchers(new AntPathRequestMatcher("/graphql/**")).permitAll()
                                        .anyRequest()
                                        .authenticated();
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        }
                );

        http.httpBasic(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(username)
                .password(passwordEncoder.encode(password))
                .roles("ADMIN");
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/actuator/prometheus");
    }
}