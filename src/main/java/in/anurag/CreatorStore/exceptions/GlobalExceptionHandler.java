package in.anurag.CreatorStore.exceptions;

import in.anurag.CreatorStore.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. Handle Resource Not Found (404)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            "The requested resource was not found in the database",
            LocalDateTime.now()),
        HttpStatus.NOT_FOUND);
  }

  // 2. Handle Bad Credentials (401 Unauthorized) <-- ADD THIS
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid username or password",
            "Please check your credentials and try again",
            LocalDateTime.now()),
        HttpStatus.UNAUTHORIZED);
  }

  // 3. Handle Access Denied (403 Forbidden)
  @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(Exception ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "You do not have permission to perform this action",
            LocalDateTime.now()),
        HttpStatus.FORBIDDEN);
  }

  // 4. Handle Validation Errors (400)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  // 5. Fallback for any other unexpected exceptions (500)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            ex.getMessage(),
            LocalDateTime.now()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
