package com.websitestudios.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 *
 * SECURITY PRINCIPLES:
 * 1. NEVER expose stack traces to the client
 * 2. NEVER expose internal class names or paths
 * 3. NEVER expose SQL queries or database details
 * 4. ALWAYS log full details server-side for debugging
 * 5. ALWAYS return a clean, safe error structure
 *
 * Handles:
 * 400 — Validation errors, bad input, malformed JSON
 * 401 — Authentication failure (Phase 6)
 * 403 — Authorization failure (Phase 6)
 * 404 — Resource not found
 * 405 — Method not allowed
 * 409 — Duplicate resource conflict
 * 415 — Unsupported media type
 * 423 — Account locked (Phase 6)
 * 429 — Rate limit exceeded (Phase 7)
 * 500 — Unexpected server errors (catch-all)
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Safe generic message — never leak internal details
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";

    // ════════════════════════════════════════════════════════════════
    // 400 — VALIDATION ERRORS (JSR-303 @Valid failures)
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Validation failed: {} field error(s)", ex.getBindingResult().getFieldErrorCount());

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            log.warn("  Field '{}': {}", fieldName, errorMessage);
        });

        WsErrorResponse errorResponse = WsErrorResponse.validationError(
                "One or more fields have invalid values",
                fieldErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 400 — MALFORMED JSON / UNREADABLE REQUEST BODY
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Malformed request body: {}", ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                400,
                "Bad Request",
                "Malformed JSON request. Please check the request body format.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 400 — MISSING REQUEST PARAMETER
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Missing request parameter: {}", ex.getParameterName());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                400,
                "Bad Request",
                "Required parameter '" + ex.getParameterName() + "' is missing.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 400 — METHOD ARGUMENT TYPE MISMATCH (e.g., id="abc")
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<WsErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch for parameter '{}': value '{}' at path {}",
                ex.getName(), ex.getValue(), request.getRequestURI());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                400,
                "Bad Request",
                "Invalid value for parameter '" + ex.getName() +
                        "'. Expected type: " + getSimpleTypeName(ex.getRequiredType()));
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 400 — INVALID INPUT (Business validation)
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<WsErrorResponse> handleInvalidInput(
            InvalidInputException ex,
            HttpServletRequest request) {

        log.warn("Invalid input: {} at path {}", ex.getMessage(), request.getRequestURI());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                400,
                "Invalid Input",
                ex.getMessage());
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 400 — CAPTCHA VERIFICATION FAILED
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(CaptchaVerificationException.class)
    public ResponseEntity<WsErrorResponse> handleCaptchaVerification(
            CaptchaVerificationException ex,
            HttpServletRequest request) {

        log.warn("CAPTCHA verification failed at path {}: {}", request.getRequestURI(), ex.getMessage());

        // Log suspicious activity — do NOT expose score to client
        if (ex.getScore() != null) {
            log.warn("  reCAPTCHA score: {} (from IP: {})", ex.getScore(), request.getRemoteAddr());
        }

        WsErrorResponse errorResponse = WsErrorResponse.of(
                400,
                "CAPTCHA Verification Failed",
                "reCAPTCHA verification failed. Please try again.");
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ════════════════════════════════════════════════════════════════
    // 401 — UNAUTHORIZED (No/Invalid authentication)
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<WsErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized access attempt at path {}: {}", request.getRequestURI(), ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                401,
                "Unauthorized",
                "Authentication is required to access this resource.");
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // ════════════════════════════════════════════════════════════════
    // 403 — FORBIDDEN (Authenticated but lacks permission)
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<WsErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {

        log.warn("Forbidden access at path {}: {}", request.getRequestURI(), ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                403,
                "Forbidden",
                "You do not have permission to perform this action.");
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // ════════════════════════════════════════════════════════════════
    // 404 — RESOURCE NOT FOUND
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WsErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found at path {}: {}", request.getRequestURI(), ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                404,
                "Not Found",
                ex.getMessage());
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ════════════════════════════════════════════════════════════════
    // 404 — SPRING'S NO RESOURCE FOUND (unknown paths)
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("No resource found: {}", ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                404,
                "Not Found",
                "The requested resource was not found.");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ════════════════════════════════════════════════════════════════
    // 405 — METHOD NOT ALLOWED
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Method not allowed: {}", ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                405,
                "Method Not Allowed",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.");

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ════════════════════════════════════════════════════════════════
    // 409 — DUPLICATE / CONFLICT
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<WsErrorResponse> handleDuplicateRequest(
            DuplicateRequestException ex,
            HttpServletRequest request) {

        log.warn("Duplicate request detected at path {}: {}", request.getRequestURI(), ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                409,
                "Conflict",
                ex.getMessage());
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ════════════════════════════════════════════════════════════════
    // 415 — UNSUPPORTED MEDIA TYPE
    // ════════════════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Unsupported media type: {}", ex.getContentType());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                415,
                "Unsupported Media Type",
                "Content type '" + ex.getContentType() + "' is not supported. Use 'application/json'.");

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ════════════════════════════════════════════════════════════════
    // 423 — ACCOUNT LOCKED
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<WsErrorResponse> handleAccountLocked(
            AccountLockedException ex,
            HttpServletRequest request) {

        log.warn("Locked account access attempt at path {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Account is temporarily locked due to multiple failed login attempts.";
        if (ex.getLockExpiresAt() != null) {
            message += " Try again after: " + ex.getLockExpiresAt();
        }

        WsErrorResponse errorResponse = WsErrorResponse.of(
                423,
                "Account Locked",
                message);
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
    }

    // ════════════════════════════════════════════════════════════════
    // 429 — RATE LIMIT EXCEEDED
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<WsErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded at path {} from IP {}: {}",
                request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());

        WsErrorResponse errorResponse = WsErrorResponse.of(
                429,
                "Too Many Requests",
                ex.getMessage());
        errorResponse.setPath(request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        return new ResponseEntity<>(errorResponse, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    // ════════════════════════════════════════════════════════════════
    // 500 — CATCH-ALL (Unexpected errors)
    // ════════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WsErrorResponse> handleAllUncaughtExceptions(
            Exception ex,
            HttpServletRequest request) {

        // Log FULL details server-side for debugging
        log.error("Unexpected error at path {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Return SAFE generic message to client
        WsErrorResponse errorResponse = WsErrorResponse.of(
                500,
                "Internal Server Error",
                INTERNAL_ERROR_MESSAGE);
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════════

    /**
     * Get a safe, simple type name for error messages.
     * Avoids exposing full class paths.
     */
    private String getSimpleTypeName(Class<?> type) {
        if (type == null)
            return "unknown";
        if (type == Long.class || type == long.class)
            return "number";
        if (type == Integer.class || type == int.class)
            return "integer";
        if (type == String.class)
            return "text";
        if (type == Boolean.class || type == boolean.class)
            return "boolean";
        return "valid value";
    }
}