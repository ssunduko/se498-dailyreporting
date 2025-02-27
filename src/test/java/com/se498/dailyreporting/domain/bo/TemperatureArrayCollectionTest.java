package com.se498.dailyreporting.domain.bo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureArrayCollectionTest {

    private Temperature freezingTemp;
    private Temperature roomTemp;
    private Temperature hotTemp;
    private Temperature boilingTemp;

    @BeforeEach
    void setUp() {
        freezingTemp = Temperature.fromFahrenheit(32.0);  // 0°C
        roomTemp = Temperature.fromFahrenheit(72.0);      // 22.22°C
        hotTemp = Temperature.fromFahrenheit(90.0);       // 32.22°C
        boilingTemp = Temperature.fromFahrenheit(212.0);  // 100°C
    }

    @Test
    void testSimpleArrayEquals() {
        Temperature[] expected = {freezingTemp, roomTemp, hotTemp};
        Temperature[] actual = {freezingTemp, roomTemp, hotTemp};

        assertArrayEquals(expected, actual, "Arrays should contain same temperature objects in same order");
    }

    @Test
    void testEmptyArrayEquals() {
        Temperature[] expected = {};
        Temperature[] actual = {};

        assertArrayEquals(expected, actual, "Empty arrays should be equal");
    }

    @Test
    void testSingleElementArrayEquals() {
        Temperature[] expected = {roomTemp};
        Temperature[] actual = {roomTemp};

        assertArrayEquals(expected, actual, "Single-element arrays should be equal");
    }

    @Test
    void testArrayEqualsWithNulls() {
        Temperature[] expected = {freezingTemp, null, hotTemp};
        Temperature[] actual = {freezingTemp, null, hotTemp};

        assertArrayEquals(expected, actual, "Arrays with null elements should be equal");
    }

    @Test
    void testArraysWithDifferentLengths() {
        Temperature[] shorter = {freezingTemp, roomTemp};
        Temperature[] longer = {freezingTemp, roomTemp, hotTemp};

        assertThrows(AssertionError.class, () ->
                assertArrayEquals(shorter, longer, "Arrays of different lengths should not be equal"));
    }

    @Test
    void testListEquals() {
        List<Temperature> expected = Arrays.asList(freezingTemp, roomTemp, hotTemp);
        List<Temperature> actual = Arrays.asList(freezingTemp, roomTemp, hotTemp);

        assertIterableEquals(expected, actual, "Lists should contain same temperature objects in same order");
    }

    @Test
    void testEmptyListEquals() {
        List<Temperature> expected = new ArrayList<>();
        List<Temperature> actual = new ArrayList<>();

        assertIterableEquals(expected, actual, "Empty lists should be equal");
    }

    @Test
    void testSetEquals() {
        Set<Temperature> expected = new HashSet<>(Arrays.asList(freezingTemp, roomTemp, hotTemp));
        Set<Temperature> actual = new HashSet<>(Arrays.asList(freezingTemp, roomTemp, hotTemp));

        assertIterableEquals(expected, actual, "Sets should contain same temperature objects");
    }

    @Test
    void testDequeEquals() {
        Deque<Temperature> expected = new ArrayDeque<>(Arrays.asList(freezingTemp, roomTemp, hotTemp));
        Deque<Temperature> actual = new ArrayDeque<>(Arrays.asList(freezingTemp, roomTemp, hotTemp));

        assertIterableEquals(expected, actual, "Deques should contain same temperature objects in same order");
    }

    @Test
    void testCollectionWithNulls() {
        List<Temperature> expected = Arrays.asList(freezingTemp, null, hotTemp);
        List<Temperature> actual = Arrays.asList(freezingTemp, null, hotTemp);

        assertIterableEquals(expected, actual, "Collections with null elements should be equal");
    }

    @Test
    void testSortedTemperatures() {
        List<Temperature> temperatures = Arrays.asList(hotTemp, freezingTemp, roomTemp, boilingTemp);
        List<Temperature> expected = Arrays.asList(freezingTemp, roomTemp, hotTemp, boilingTemp);

        temperatures.sort(Comparator.comparing(Temperature::getFahrenheit));
        assertIterableEquals(expected, temperatures, "Temperatures should be sorted by Fahrenheit value");
    }

    @Test
    void testTemperatureArraySorting() {
        Temperature[] temperatures = {hotTemp, freezingTemp, roomTemp, boilingTemp};
        Temperature[] expected = {freezingTemp, roomTemp, hotTemp, boilingTemp};

        Arrays.sort(temperatures, Comparator.comparing(Temperature::getFahrenheit));
        assertArrayEquals(expected, temperatures, "Temperature array should be sorted by Fahrenheit value");
    }

    @Test
    void testCollectionSize() {
        List<Temperature> temperatures = Arrays.asList(freezingTemp, roomTemp, hotTemp);
        assertEquals(3, temperatures.size(), "Collection should contain exactly 3 temperatures");
    }

    @Test
    void testCollectionContains() {
        Set<Temperature> temperatures = new HashSet<>(Arrays.asList(freezingTemp, roomTemp, hotTemp));
        assertTrue(temperatures.contains(roomTemp), "Collection should contain room temperature");
        assertFalse(temperatures.contains(boilingTemp), "Collection should not contain boiling temperature");
    }

    @Test
    void testArrayCopy() {
        Temperature[] original = {freezingTemp, roomTemp, hotTemp};
        Temperature[] copy = Arrays.copyOf(original, original.length);

        assertArrayEquals(original, copy, "Copied array should equal original array");
        assertNotSame(original, copy, "Copied array should be a different object than original");
    }

    @Test
    void testSubList() {
        List<Temperature> fullList = Arrays.asList(freezingTemp, roomTemp, hotTemp, boilingTemp);
        List<Temperature> subList = fullList.subList(1, 3);
        List<Temperature> expected = Arrays.asList(roomTemp, hotTemp);

        assertIterableEquals(expected, subList, "Sublist should contain only selected temperatures");
    }
}