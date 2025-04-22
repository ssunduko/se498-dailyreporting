
package com.se498.dailyreporting;

import com.se498.dailyreporting.domain.bo.WeatherRecord;
import com.se498.dailyreporting.repository.WeatherRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestDailyReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDailyReportingApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(WeatherRecordRepository weatherRecordRepository) {
        return args -> {
            System.out.println("Test application initialized. Beans provided by Spring Boot ready for testing.");

            // Additional setup for testing can be added here if needed
        };
    }
}