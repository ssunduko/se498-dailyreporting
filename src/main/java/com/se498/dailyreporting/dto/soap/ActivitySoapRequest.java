package com.se498.dailyreporting.dto.soap;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ActivitySoapRequest - Request object for activity operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivitySoapRequest", namespace = "http://reporting.construction.com/soap")
public class ActivitySoapRequest {

    @XmlElement
    private String activityId;

    @XmlElement(required = true)
    private String reportId;

    @XmlElement(required = true)
    private String description;

    @XmlElement(required = true)
    private String category;

    @XmlElement(required = true)
    private String startTime;

    @XmlElement(required = true)
    private String endTime;

    @XmlElement(required = true)
    private double progress;

    @XmlElement(required = true)
    private String status;

    @XmlElement
    private String notes;

    @XmlElement
    private String personnel;

    @XmlElement(required = true)
    private String username;
}