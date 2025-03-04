package com.se498.dailyreporting.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DailyReportSoapResponse - Response object for daily report operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DailyReportSoapResponse", namespace = "http://reporting.construction.com/soap")
public class DailyReportSoapResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private String id;

    @XmlElement
    private String projectId;

    @XmlElement
    private String reportDate;

    @XmlElement
    private String status;

    @XmlElement
    private String notes;

    @XmlElement
    private String createdAt;

    @XmlElement
    private String createdBy;

    @XmlElement
    private String updatedAt;

    @XmlElement
    private String updatedBy;

    @XmlElement
    private double progress;

    @XmlElement
    private boolean complete;
}