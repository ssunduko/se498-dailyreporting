package com.se498.dailyreporting.soap.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * XmlAdapter for converting between LocalDate and String for JAXB marshalling/unmarshalling
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate unmarshal(String value) {
        return value != null ? LocalDate.parse(value, formatter) : null;
    }

    @Override
    public String marshal(LocalDate value) {
        return value != null ? value.format(formatter) : null;
    }
}
