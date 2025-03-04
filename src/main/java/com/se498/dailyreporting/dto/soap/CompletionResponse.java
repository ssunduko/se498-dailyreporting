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
 * CompletionResponse - Response object for operations that return completion status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletionResponse", namespace = "http://reporting.construction.com/soap")
public class CompletionResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private boolean complete;

    @XmlElement
    private String message;
}