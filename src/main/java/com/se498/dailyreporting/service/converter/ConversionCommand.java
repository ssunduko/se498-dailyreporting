package com.se498.dailyreporting.service.converter;

/**
 * Command interface for conversion operations
 */
public interface ConversionCommand {
    /**
     * Execute the conversion
     * @return Conversion result
     */
    Double execute();
}
