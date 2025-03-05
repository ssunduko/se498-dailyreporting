package com.se498.dailyreporting.config;

import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for the gRPC server
 */
@Slf4j
@Configuration
public class GrpcServerConfig {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Autowired PasswordEncoder passwordEncoder;


    /**
     * gRPC Server logging interceptor
     * Logs all incoming requests
     */
    @GrpcGlobalServerInterceptor
    public ServerInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }

    /**
     * Authentication reader for gRPC Basic Authentication
     */
    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new BasicGrpcAuthenticationReader();
    }

    /**
     * User details service with in-memory authentication
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Password encoder for user authentication
     */
    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

    /**
     * Authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider()));
    }

    /**
     * Security metadata source for gRPC endpoints
     * Configures which endpoints require authentication
     */
    /*@Bean
    public GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();

        // Secure all report modification endpoints with ADMIN role
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/CreateReport", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/UpdateReport", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/SubmitReport", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/ApproveReport", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/RejectReport", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/DeleteReport", AccessPredicate.hasRole("ADMIN"));

        // Secure all activity modification endpoints with ADMIN role
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/AddActivity", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/UpdateActivity", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/UpdateActivityProgress", AccessPredicate.hasRole("ADMIN"));
        source.set("com.se498.dailyreporting.grpc.DailyReportingService/DeleteActivity", AccessPredicate.hasRole("ADMIN"));

        // Allow read-only operations to be accessed by any authenticated user
        source.setDefault(AccessPredicate.authenticated());

        return source;
    }*/
}