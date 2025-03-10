package com.se498.dailyreporting.service.converter;


import java.util.Map;
import java.util.Scanner;

public class TemperatureConverter extends AbstractTemperatureConverter {

    private static final TemperatureConverter instance = new TemperatureConverter();

    public Double convert(double numberToConvert) {
        return numberToConvert * 9/5 + 32;
    }
    public Double convert(double numberToConvert, String conversionScale){
        if (conversionScale.equalsIgnoreCase("Celsius to Fahrenheit")){
            return convert(numberToConvert);
        } else {
            System.out.println("Invalid Scale");
        }
        return null;
    }

    @Override
    protected ConversionStrategy getStrategyByName(String strategyName) {
        return null;
    }

    @Override
    public Map<String, ConversionStrategy> getAvailableStrategies() {
        return null;
    }

    private static double readNumber(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter Number To Convert: ");
        return scanner.nextDouble();
    }
    private static String readConversionScale(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter Conversion Scale: ");
        return scanner.nextLine();

    }
    public static void main(String[] args) throws Exception {
        double numberToConvert = readNumber();
        String conversionScale = readConversionScale();
        System.out.println("Converted Number: " + instance.convert(numberToConvert, conversionScale));
    }
}
