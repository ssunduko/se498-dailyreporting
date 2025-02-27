package com.se498.dailyreporting.domain.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Location value object
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Location {
    private String city;
    private String country;
    private String stateOrProvince;
    private String zipCode;

    // Regex pattern for basic US zip code validation
    private static final Pattern US_ZIP_PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");

    /**
     * Constructor with city and country
     */
    public Location(String city, String country) {
        this(city, country, null, null);
    }

    /**
     * Constructor with city, country and state/province
     */
    public Location(String city, String country, String stateOrProvince) {
        this(city, country, stateOrProvince, null);
    }

    /**
     * Factory method to create a location from zip code
     *
     * @param zipCode The zip/postal code
     * @param country The country code (default "US" if null)
     * @return A new Location instance
     */
    public static Location fromZipCode(String zipCode, String country) {
        String countryCode = country != null ? country : "US";
        return new Location(null, countryCode, null, zipCode);
    }

    /**
     * Check if this location is primarily identified by zip code
     */
    public boolean isZipBased() {
        return zipCode != null && !zipCode.isEmpty();
    }

    /**
     * Validate if the location contains sufficient information for lookup
     */
    public boolean isValid() {
        // Valid if we have either a city+country or a zip code
        boolean hasCityCountry = city != null && !city.trim().isEmpty() &&
                country != null && !country.trim().isEmpty();
        boolean hasZip = zipCode != null && !zipCode.trim().isEmpty();

        return hasCityCountry || hasZip;
    }

    /**
     * Validate zip code format based on country
     */
    public boolean hasValidZipFormat() {
        if (zipCode == null || zipCode.isEmpty()) {
            return false;
        }

        // For US zip codes, validate using pattern
        if ("US".equalsIgnoreCase(country)) {
            return US_ZIP_PATTERN.matcher(zipCode).matches();
        }

        // For other countries, just ensure it's not empty
        // Future enhancement: add validation patterns for other countries
        return true;
    }

    /**
     * Formats the location as a user-friendly string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Add city if available
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }

        // Add state/province if available
        if (stateOrProvince != null && !stateOrProvince.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(stateOrProvince);
        }

        // Add zip code if available and not already included
        if (zipCode != null && !zipCode.isEmpty() && !sb.toString().contains(zipCode)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(zipCode);
        }

        // Add country if available
        if (country != null && !country.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(country);
        }

        // If nothing was added, try to use zip code
        if (sb.isEmpty() && zipCode != null && !zipCode.isEmpty()) {
            sb.append("Zip ").append(zipCode);
            if (country != null && !country.equals("US")) {
                sb.append(", ").append(country);
            }
        }

        return sb.toString();
    }

    /**
     * Returns a location string suitable for API queries
     */
    public String toQueryString() {
        // If zip-based, return zip code
        if (isZipBased()) {
            return zipCode + "," + (country != null ? country : "US");
        }

        // Otherwise, use city-based format
        StringBuilder sb = new StringBuilder(city);

        if (stateOrProvince != null && !stateOrProvince.isEmpty()) {
            sb.append(",").append(stateOrProvince);
        }

        if (country != null && !country.isEmpty()) {
            sb.append(",").append(country);
        }

        return sb.toString();
    }
}