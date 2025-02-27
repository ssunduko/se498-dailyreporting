package com.se498.dailyreporting.config;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class SwaggerConfig {

    @Value("${openapi.prod-url}")
    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() {

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);

        Contact contact = new Contact();
        contact.setEmail("ssunduko@gmail.com");
        contact.setName("Sergey Sundukovskiy, Ph.D.");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Swagger API for SE498-SAMPLE-DAILY-REPORTING-APP")
                .version("1.0")
                .contact(contact)
                .description("Spring Boot Swagger API Example")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(prodServer));
    }
}