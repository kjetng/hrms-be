package org.httt2.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "org.httt2.hrms.bonus")
public class BonusSettingsExceptionHandler {

    /**
     * DTO validation errors
     * (@NotNull, @Min, @Max, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(errors);
    }

        /**
         * Business rule violations (e.g., insufficient points).
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
                Map<String, String> body = new HashMap<>();
                body.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        /**
         * Authorization failures specific to bonus operations.
         */
        @ExceptionHandler(SecurityException.class)
        public ResponseEntity<Map<String, String>> handleSecurity(SecurityException ex) {
                Map<String, String> body = new HashMap<>();
                body.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
}
