package ru.yandex.kardomoblieapp.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNotAuthorizedException(NotAuthorizedException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidException(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        Map<String, String> exceptions = errorResponse.getErrors();
        for (ObjectError oe : e.getBindingResult().getAllErrors()) {
            exceptions.put(oe.getObjectName(), oe.getDefaultMessage());
            log.error("Объект {} не прошел валидацию. Причина: {}.", oe.getObjectName(), oe.getDefaultMessage());
        }
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            exceptions.put(error.getField(), error.getDefaultMessage());
            log.error("Поле {} не прошло валидацию. Причина: {}.", error.getField(), error.getDefaultMessage());
        }
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConversionFailedException(MethodArgumentTypeMismatchException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("Неизвестное поле.", String.valueOf(e.getValue()));
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put(e.getHeaderName(), e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put(e.getParameterName(), e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectEventDatesException(IncorrectEventDatesException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("acceptable MIME type:", MediaType.APPLICATION_JSON_VALUE);
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataFileStorageException(DataFileStorageException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error while managing file", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("error", e.getLocalizedMessage());
        errorResponse.getErrors().put("Maximum upload size:", String.valueOf(e.getMaxUploadSize()));
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        errorResponse.getErrors().put("stackTrace", getStackTraceAsString(e));
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
