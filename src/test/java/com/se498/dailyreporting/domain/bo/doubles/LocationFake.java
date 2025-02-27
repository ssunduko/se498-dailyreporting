package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Fake test double for Location
 * Implements simplified in-memory storage and validation
 */
public class LocationFake extends Location {
    private String city;
    private String country;
    private String stateOrProvince;
    private String zipCode;

    // In-memory storage for zip code lookups
    private static final Map<String, LocationData> ZIP_DATABASE = new HashMap<>();

    static {
        // Populate some test zip codes
        ZIP_DATABASE.put("10001", new LocationData("New York", "NY", "US"));
        ZIP_DATABASE.put("90210", new LocationData("Beverly Hills", "CA", "US"));
        ZIP_DATABASE.put("98101", new LocationData("Seattle", "WA", "US"));
        ZIP_DATABASE.put("60601", new LocationData("Chicago", "IL", "US"));
    }

    // Static inner class to hold location data
    private static class LocationData {
        String city;
        String state;
        String country;

        LocationData(String city, String state, String country) {
            this.city = city;
            this.state = state;
            this.country = country;
        }
    }

    // Constructor for city-based location
    public LocationFake(String city, String stateOrProvince, String country) {
        validateCityInput(city, country);
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.country = country;
    }

    // Constructor for zip code-based location
    public LocationFake(String zipCode, String country) {
        validateZipInput(zipCode, country);
        this.zipCode = zipCode;
        this.country = country;

        // Look up city and state from zip database
        LocationData data = ZIP_DATABASE.get(zipCode);
        if (data != null) {
            this.city = data.city;
            this.stateOrProvince = data.state;
        }
    }

    // Factory method mimicking the real Location.fromZipCode
    public static LocationFake fromZipCode(String zipCode, String country) {
        return new LocationFake(zipCode, country);
    }

    // Basic validation methods
    private void validateCityInput(String city, String country) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }

    private void validateZipInput(String zipCode, String country) {
        if (zipCode == null || !zipCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    @Override
    public String getZipCode() {
        return zipCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(city);

        if (stateOrProvince != null) {
            sb.append(", ").append(stateOrProvince);
        }

        if (zipCode != null) {
            sb.append(" ").append(zipCode);
        }

        sb.append(", ").append(country);

        return sb.toString();
    }
}
