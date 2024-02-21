package io.kamenskiy.situations.exception_handling.advice;

import io.kamenskiy.situations.exception_handling.annotation.CustomExceptionHandler;
import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice(annotations = CustomExceptionHandler.class)
public class CustomControllerAdvice {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response> handlerException(BusinessException ex){
        String message = String.format("%s %s", LocalDateTime.now(), ex.getMessage());
        Response response = new Response(message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
