package com.se498.dailyreporting.soap;

import com.se498.dailyreporting.controller.DailyReportSoapController;
import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Utility component to generate WSDL files at startup
 * Only runs in non-test profiles to avoid issues during testing
 */
@Slf4j
@Component
@Profile("!test")
public class WsdlGenerator {

    @Autowired
    private Bus bus;

    @Autowired
    private DailyReportSoapController dailyReportSoapController;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${wsdl.output.path:src/main/resources/wsdl}")
    private String wsdlOutputPath;

    /**
     * Logs WSDL information at application startup
     */
    @PostConstruct
    public void logWsdlInfo() {
        try {
            // Create directory if it doesn't exist
            File dir = new File(wsdlOutputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Log WSDL URL information
            logWsdlForService(
                    "DailyReportService",
                    "http://localhost:" + serverPort + "/ws/soap/dailyReport");

        } catch (Exception e) {
            // Log error but don't fail startup
            log.error("Error handling WSDL files: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs WSDL URL information for a specific service
     *
     * @param serviceName The service name
     * @param address The service endpoint address
     */
    private void logWsdlForService(String serviceName, String address) {
        // Output the WSDL path for logging
        log.info("WSDL URL: {}", address + "?wsdl");
        log.info("To access WSDL, start the application and navigate to: {}", address + "?wsdl");

        // Log the output path
        log.info("Static WSDL would be generated at: {}",
                new File(wsdlOutputPath, serviceName + ".wsdl").getAbsolutePath());
    }
}