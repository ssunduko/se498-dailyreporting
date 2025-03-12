package com.se498.dailyreporting.service.solid;

import java.util.Scanner;

public class NaiveTemperatureConverter {

    public static Double convert(double numberToConvert) {
        return numberToConvert * 9/5 + 32;
    }
    public static Double convert(double numberToConvert, String conversionScale){
        if (conversionScale.equalsIgnoreCase("Celsius to Fahrenheit")){
            return convert(numberToConvert);
        } else {
            System.out.println("Invalid Scale");
        }
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
        System.out.println("Converted Number: " + convert(numberToConvert, conversionScale));
    }
}