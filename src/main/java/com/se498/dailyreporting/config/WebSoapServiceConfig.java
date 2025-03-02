package com.se498.dailyreporting.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Test configuration for SOAP WebService tests
 */
@Configuration
public class WebSoapServiceConfig {

    /**
     * Creates a JAXB marshaller for SOAP messages
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Set the context path to the generated JAXB classes
        marshaller.setContextPath("com.se498.dailyreporting.soap.gen");
        return marshaller;
    }

    /**
     * Creates a WebServiceTemplate for testing SOAP endpoints
     */
    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri("http://localhost:8080/ws");


        return webServiceTemplate;
    }
}