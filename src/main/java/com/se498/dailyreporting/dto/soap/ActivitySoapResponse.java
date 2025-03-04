package com.se498.dailyreporting.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ActivitySoapResponse - Response object for activity operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivitySoapResponse", namespace = "http://reporting.construction.com/soap")
public class ActivitySoapResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private String id;

    @XmlElement
    private String reportId;

    @XmlElement
    private String description;

    @XmlElement
    private String category;

    @XmlElement
    private String startTime;

    @XmlElement
    private String endTime;

    @XmlElement
    private double progress;

    @XmlElement
    private String status;

    @XmlElement
    private String notes;

    @XmlElement
    private String personnel;

    @XmlElement
    private String createdAt;

    @XmlElement
    private String createdBy;

    @XmlElement
    private String updatedAt;

    @XmlElement
    private long durationMinutes;
}