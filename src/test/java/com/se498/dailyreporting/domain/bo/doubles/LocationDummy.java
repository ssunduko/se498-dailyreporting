package com.se498.dailyreporting.domain.bo.doubles;

import com.se498.dailyreporting.domain.bo.Location;

/**
 * Dummy test double for Location class
 * Returns fixed dummy values for all methods
 */
public class LocationDummy extends Location {

    private static final String DUMMY_CITY = "DummyCity";
    private static final String DUMMY_COUNTRY = "DC";
    private static final String DUMMY_STATE = "DummyState";
    private static final String DUMMY_ZIP = "00000";

    public LocationDummy() {
        super();
    }

    @Override
    public String getCity() {
        return DUMMY_CITY;
    }

    @Override
    public String getCountry() {
        return DUMMY_COUNTRY;
    }

    @Override
    public String getStateOrProvince() {
        return DUMMY_STATE;
    }

    @Override
    public String getZipCode() {
        return DUMMY_ZIP;
    }

    @Override
    public String toString() {
        return String.format("%s, %s %s, %s", DUMMY_CITY, DUMMY_STATE, DUMMY_ZIP, DUMMY_COUNTRY);
    }
}