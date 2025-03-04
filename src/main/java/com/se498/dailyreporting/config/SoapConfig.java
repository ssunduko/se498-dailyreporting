package com.se498.dailyreporting.config;

import com.se498.dailyreporting.controller.DailyReportSoapController;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for SOAP Web Services
 */
@Configuration
@Slf4j
public class SoapConfig {

    @Autowired
    private DailyReportSoapController dailyReportSoapController;

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        return new ServletRegistrationBean<>(new CXFServlet(), "/soap/*");
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public Endpoint dailyReportEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), dailyReportSoapController);
        endpoint.publish("/DailyReportService");
        log.info("Published SOAP endpoint at: /soap/DailyReportService");
        return endpoint;
    }
}