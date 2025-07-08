package com.ebanking.unit.exception;

import com.ebanking.dto.ErrorResponse;
import com.ebanking.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private ServletWebRequest servletWebRequest;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle AuthenticationException correctly")
    void shouldHandleAuthenticationException() {
        // Given
        AuthenticationException ex = new AuthenticationException("Invalid credentials") {};
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication is required to access this resource");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/transactions");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle AccessDeniedException correctly")
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/admin");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to access this resource");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/admin");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException correctly")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        // Given
        FieldError fieldError = new FieldError("transactionRequest", "amount", "Amount must be greater than 0");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Create a proper MethodParameter
        Method method = TestController.class.getMethod("testMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors().get(0).getField()).isEqualTo("amount");
        assertThat(response.getBody().getValidationErrors().get(0).getMessage()).isEqualTo("Amount must be greater than 0");
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException correctly")
    void shouldHandleMethodArgumentTypeMismatchException() {
        // Given
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid", Integer.class, "amount", null, new RuntimeException("Type mismatch"));
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("Invalid value for parameter 'amount'");
        assertThat(response.getBody().getMessage()).contains("Expected type: Integer");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException correctly")
    void shouldHandleConstraintViolationException() {
        // Given
        ConstraintViolation<String> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("baseCurrency");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must match the pattern '^[A-Z]{3}$'");
        when(violation.getInvalidValue()).thenReturn("USD");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex, servletWebRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors().get(0).getField()).isEqualTo("baseCurrency");
        assertThat(response.getBody().getValidationErrors().get(0).getMessage()).isEqualTo("must match the pattern '^[A-Z]{3}$'");
        assertThat(response.getBody().getValidationErrors().get(0).getRejectedValue()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException correctly")
    void shouldHandleMissingServletRequestParameterException() {
        // Given
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("amount", "BigDecimal");
        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingServletRequestParameterException(ex, servletWebRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Required parameter 'amount' of type 'BigDecimal' is missing");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/transactions");
    }

    @Test
    @DisplayName("Should handle DateTimeParseException correctly")
    void shouldHandleDateTimeParseException() {
        // Given
        DateTimeParseException ex = new DateTimeParseException("Text '2023-13-45' could not be parsed", "2023-13-45", 0);
        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDateTimeParseException(ex, servletWebRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid date/time format: '2023-13-45'. Expected format: yyyy-MM-dd");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/transactions");
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void shouldHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error occurred");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/transactions");
    }

    @Test
    @DisplayName("Should handle multiple validation errors correctly")
    void shouldHandleMultipleValidationErrors() throws NoSuchMethodException {
        // Given
        FieldError amountError = new FieldError("transactionRequest", "amount", "Amount must be greater than 0");
        FieldError currencyError = new FieldError("transactionRequest", "baseCurrency", "Currency must be 3 characters");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(amountError, currencyError));

        // Create a proper MethodParameter
        Method method = TestController.class.getMethod("testMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors())
                .extracting("field")
                .containsExactlyInAnyOrder("amount", "baseCurrency");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with null invalid value")
    void shouldHandleConstraintViolationExceptionWithNullInvalidValue() {
        // Given
        ConstraintViolation<String> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("baseCurrency");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");
        when(violation.getInvalidValue()).thenReturn(null);

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex, servletWebRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors().get(0).getRejectedValue()).isNull();
    }

    @Test
    @DisplayName("Should handle empty validation errors correctly")
    void shouldHandleEmptyValidationErrors() throws NoSuchMethodException {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        // Create a proper MethodParameter
        Method method = TestController.class.getMethod("testMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with null value")
    void shouldHandleMethodArgumentTypeMismatchExceptionWithNullValue() {
        // Given
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                null, Integer.class, "amount", null, new RuntimeException("Type mismatch"));
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Invalid value for parameter 'amount'");
        assertThat(response.getBody().getMessage()).contains("null");
    }

    @Test
    @DisplayName("Should handle DateTimeParseException with complex error message")
    void shouldHandleDateTimeParseExceptionWithComplexMessage() {
        // Given
        DateTimeParseException ex = new DateTimeParseException(
                "Text 'invalid-date' could not be parsed at index 0", "invalid-date", 0);
        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDateTimeParseException(ex, servletWebRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid date/time format: 'invalid-date'. Expected format: yyyy-MM-dd");
    }

    @Test
    @DisplayName("Should verify timestamp is set for all error responses")
    void shouldVerifyTimestampIsSetForAllErrorResponses() {
        // Given
        Exception ex = new RuntimeException("Test error");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    /**
     * Helper class for creating MethodParameter instances in tests.
     */
    private static class TestController {
        public void testMethod(String param) {
            // This method is only used for creating MethodParameter instances in tests
        }
    }
}