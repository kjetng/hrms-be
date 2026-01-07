package org.httt2.hrms.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> fieldErrors = new LinkedHashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });

    return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, fieldErrors);
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidPasswordException(
      InvalidPasswordException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PasswordMismatchException.class)
  public ResponseEntity<Map<String, Object>> handlePasswordMismatchException(
      PasswordMismatchException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IncorrectPasswordException.class)
  public ResponseEntity<Map<String, Object>> handleIncorrectPasswordException(
      IncorrectPasswordException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
      UsernameNotFoundException ex) {
    return buildErrorResponse("Unauthorized", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<Map<String, Object>> handleExpiredJwtException(
      ExpiredJwtException ex) {
    return buildErrorResponse("Token has expired. Please login again.", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
  public ResponseEntity<Map<String, Object>> handleInvalidJwtException(
      JwtException ex) {
    return buildErrorResponse("Invalid token. Please login again.", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<Map<String, Object>> handleJwtException(
      JwtException ex) {
    return buildErrorResponse("Token validation failed. Please login again.", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(
      RuntimeException ex) {
    // Check if it's a password update failure
    if (ex.getMessage() != null && ex.getMessage().contains("Failed to update password")) {
      return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Re-throw if it's not a handled exception
    throw ex;
  }


  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      String message, HttpStatus status) {
    return buildErrorResponse(message, status, null);
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      String message, HttpStatus status, Map<String, String> errors) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);

    if (errors != null && !errors.isEmpty()) {
      body.put("details", errors);
    }

    return new ResponseEntity<>(body, status);
  }
}
