package com.se498.dailyreporting.service.converter;

import java.util.HashMap;
import java.util.Map;

public class TemperatureConverter {

    private static final Map<Double,Double> db = new HashMap(){{
        put(0.0,32.0);
        put(1.0,33.8);
        put(2.0,35.6);
    }};
    double convert (double numberToConvert) {
        return numberToConvert * 9/5 + 32;
    }
}
