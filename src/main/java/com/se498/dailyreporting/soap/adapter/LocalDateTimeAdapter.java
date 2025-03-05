package com.se498.dailyreporting.soap.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XmlAdapter for converting between LocalDateTime and String for JAXB marshalling/unmarshalling
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime unmarshal(String value) {
        return value != null ? LocalDateTime.parse(value, formatter) : null;
    }

    @Override
    public String marshal(LocalDateTime value) {
        return value != null ? value.format(formatter) : null;
    }
}
