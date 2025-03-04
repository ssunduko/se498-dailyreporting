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
 * ReportListResponse - Response object for operations that return multiple reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportListResponse", namespace = "http://reporting.construction.com/soap")
public class ReportListResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement
    private String errorMessage;

    @XmlElement(required = true)
    private int reportCount;

    @XmlElement
    private List<DailyReportSoapResponse> reports = new ArrayList<>();
}