package com.se498.dailyreporting.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ServiceResponse - Generic response for operations that only need success/failure feedback
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceResponse", namespace = "http://reporting.construction.com/soap")
public class ServiceResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private String message;
}