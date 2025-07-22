package uz.pdp.online_education.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.errors.FieldErrorDTO;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

/**
 * A global exception handler to catch exceptions from all controllers.
 * It provides a consistent, structured JSON response for any error,
 * which simplifies front-end error handling.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseDTO<Object>> handleExpiredJwtException(ExpiredJwtException e) {
        // ErrorDTO'ni yaratamiz
        ErrorDTO error = new ErrorDTO(401, "Tokenning yashash muddati tugagan. Iltimos, qayta tizimga kiring.");

        // ResponseDTO.error() static metodi orqali javobni yaratamiz
        ResponseDTO<Object> response = ResponseDTO.error(error);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ResponseDTO<Object>> handleSignatureException(SignatureException e) {
        ErrorDTO error = new ErrorDTO(401, "Yaroqsiz JWT imzosi. Token buzilgan yoki o'zgartirilgan bo'lishi mumkin.");
        ResponseDTO<Object> response = ResponseDTO.error(error);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ResponseDTO<Object>> handleMalformedJwtException(MalformedJwtException e) {
        ErrorDTO error = new ErrorDTO(401, "Noto'g'ri formatdagi JWT token.");
        ResponseDTO<Object> response = ResponseDTO.error(error);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseDTO<Object>> handleJwtException(JwtException e) {
        ErrorDTO error = new ErrorDTO(401, "JWT token bilan bog'liq xatolik: " + e.getMessage());
        ResponseDTO<Object> response = ResponseDTO.error(error);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Handles cases where the request body is missing or malformed (not readable).
     * This is a common issue when the client sends an empty body for a POST/PUT request
     * that requires one, or sends malformed JSON.
     *
     * @param ex The HttpMessageNotReadableException instance.
     * @return A ResponseEntity with a 400 Bad Request status and a clear message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDTO<Object>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Failed to read request body: {}", ex.getMessage());

        ErrorDTO error = new ErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "So'rov tanasi (request body) mavjud emas yoki noto'g'ri formatda."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.error(error));
    }


    /**
     * Handles data conflict errors, like a duplicate username or email.
     * This handler is specifically for our custom DataConflictException.
     *
     * @param ex The DataConflictException instance.
     * @return A ResponseEntity with a 409 Conflict status.
     */
    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDataConflict(DataConflictException ex) {
        log.warn("Data conflict: {}", ex.getMessage());
        ErrorDTO error = new ErrorDTO(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseDTO.error(error));
    }

    /**
     * Handles validation errors from @Valid annotation.
     *
     * @param ex The exception thrown when validation fails.
     * @return A ResponseEntity with a 400 Bad Request status and detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.add(new FieldErrorDTO(fieldError.getField(), fieldError.getDefaultMessage()))
        );

        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.error(error));
    }

    /**
     * Handles cases where an entity is not found in the database.
     *
     * @param ex Your custom EntityNotFoundException.
     * @return A ResponseEntity with a 404 NotFound status.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        ErrorDTO error = new ErrorDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.error(error));
    }

    /**
     * Handles bad credentials exceptions during login.
     *
     * @param ex The exception thrown for incorrect username or password.
     * @return A ResponseEntity with a 401 Unauthorized status.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ErrorDTO error = new ErrorDTO(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()/*"username or password incorrect"*/);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error(error));
    }

    /**
     * Handles access denied exceptions when a user tries to access a resource
     * they are not authorized to.
     *
     * @param ex The AccessDeniedException.
     * @return A ResponseEntity with a 403 Forbidden status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        ErrorDTO error = new ErrorDTO(HttpStatus.FORBIDDEN.value(), "Ruxsat yo'q. Bu amalni bajarish uchun sizda yetarli huquqlar mavjud emas.");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseDTO.error(error));
    }

    /**
     * A catch-all handler for any other unhandled exceptions.
     * This is a critical safety net.
     *
     * @param ex The generic exception.
     * @return A ResponseEntity with a 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleAllOtherExceptions(Exception ex) {
        // MUHIM: Har doim kutilmagan xatolikni log faylga to'liq yozing!
        log.error("An unexpected error occurred", ex);

        ErrorDTO error = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Tizimda kutilmagan ichki xatolik yuz berdi.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(error));
    }
}