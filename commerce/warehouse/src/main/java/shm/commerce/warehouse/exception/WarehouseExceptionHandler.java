package shm.commerce.warehouse.exception;

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
import shm.commerce.warehouse.dto.ApiErrorDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class WarehouseExceptionHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<ApiErrorDto> handleAlreadyInWarehouse(
            SpecifiedProductAlreadyInWarehouseException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "The specified product is already registered in warehouse.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<ApiErrorDto> handleProductNotInWarehouse(
            NoSpecifiedProductInWarehouseException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "The specified product is not registered in warehouse.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouseException.class)
    public ResponseEntity<ApiErrorDto> handleLowQuantity(
            ProductInShoppingCartLowQuantityInWarehouseException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "The requested products are not available in the required quantity.",
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

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            WarehouseValidationException.class
    })
    public ResponseEntity<ApiErrorDto> handleValidation(Exception exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage()
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
