package com.se498.dailyreporting.config;

import com.se498.dailyreporting.controller.DailyReportSoapController;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for JAX-WS SOAP web services using Apache CXF
 * Only enabled in non-test profiles to avoid conflicts with test configurations
 */
@Configuration
//@Profile("!test") // Don't run this config in test profile
public class SoapWebServiceConfig {

    @Autowired
    private Bus bus;

    @Autowired
    private DailyReportSoapController dailyReportSoapController;

    /**
     * Configures and publishes the DailyReportService SOAP endpoint
     */
    @Bean
    public Endpoint dailyReportServiceEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, dailyReportSoapController);
        endpoint.publish("/dailyReport");
        return endpoint;
    }
}