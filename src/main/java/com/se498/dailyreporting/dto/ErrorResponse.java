package com.se498.dailyreporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<ValidationError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public void addValidationError(String field, String message, Object rejectedValue) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build());
    }
}