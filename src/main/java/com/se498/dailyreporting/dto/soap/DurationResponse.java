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
 * DurationResponse - Response object for operations that return duration value
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DurationResponse", namespace = "http://reporting.construction.com/soap")
public class DurationResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private long durationMinutes;

    @XmlElement
    private String message;
}