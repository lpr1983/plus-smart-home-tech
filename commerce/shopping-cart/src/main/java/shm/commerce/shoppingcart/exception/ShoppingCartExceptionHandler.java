package shm.commerce.shoppingcart.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import shm.commerce.shoppingcart.dto.ApiErrorDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ShoppingCartExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiErrorDto> handleNotAuthorized(NotAuthorizedUserException exception) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "The user is not authorized.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<ApiErrorDto> handleNoProducts(NoProductsInShoppingCartException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "The requested products are not present in the shopping cart.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ShoppingCartDeactivatedException.class)
    public ResponseEntity<ApiErrorDto> handleDeactivated(ShoppingCartDeactivatedException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "The shopping cart cannot be changed.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolation(
            ConstraintViolationException exception) {
        String errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errors
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDto> handleMissingRequestParameter(
            MissingServletRequestParameterException exception) {
        if ("username".equals(exception.getParameterName())) {
            return buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    "The user is not authorized.",
                    "Username must not be blank."
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            ShoppingCartValidationException.class
    })
    public ResponseEntity<ApiErrorDto> handleValidation(Exception exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorDto> handleFeignException(FeignException exception) {
        HttpStatus status = HttpStatus.resolve(exception.status());
        if (status == null) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        String message = exception.contentUTF8();
        if (message == null || message.isBlank()) {
            message = exception.getMessage();
        }

        return buildResponse(
                status,
                "Warehouse request failed.",
                message
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleInternal(Exception exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal unknown server error.",
                exception.getMessage()
        );
    }

    private ResponseEntity<ApiErrorDto> buildResponse(
            HttpStatus status,
            String reason,
            String message) {
        ApiErrorDto error = new ApiErrorDto();
        error.setStatus(status.name());
        error.setReason(reason);
        error.setMessage(message);
        error.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(status).body(error);
    }
}
