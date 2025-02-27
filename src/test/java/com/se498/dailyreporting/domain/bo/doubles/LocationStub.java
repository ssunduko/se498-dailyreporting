package com.se498.dailyreporting.domain.bo.doubles;


import com.se498.dailyreporting.domain.bo.Location;

/**
 * Stub test double for Location
 * Returns predefined values with minimal implementation
 */
public class LocationStub extends Location {

    @Override
    public String getCity() {
        return "StubCity";
    }

    @Override
    public String getCountry() {
        return "SC";
    }

    @Override
    public String getStateOrProvince() {
        return "StubState";
    }

    @Override
    public String getZipCode() {
        return "12345";
    }

    @Override
    public String toString() {
        return "StubCity, StubState 12345, SC";
    }

    // Stubbed method to confirm location with parameter - always returns false
    public boolean confirmLocation(Location location) {
        return false;
    }

    // Static factory method to match Location.fromZipCode behavior
    public static LocationStub createWithZip(String zipCode, String country) {
        return new LocationStub();
    }
}