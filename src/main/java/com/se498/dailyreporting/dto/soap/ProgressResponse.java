package com.se498.dailyreporting.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProgressResponse - Response object for operations that return progress value
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProgressResponse", namespace = "http://reporting.construction.com/soap")
public class ProgressResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private double progress;

    @XmlElement
    private String message;
}

