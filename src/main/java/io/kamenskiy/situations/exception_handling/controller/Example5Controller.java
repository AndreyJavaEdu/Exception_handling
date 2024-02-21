package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.annotation.CustomExceptionHandler;
import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CustomExceptionHandler
public class Example5Controller {

    @GetMapping(value = "/testCustomAdvice", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testCustomControllerAdvice(@RequestParam(required = false, defaultValue = "false") boolean exception)
        throws BusinessException{
        if (exception){
            throw new BusinessException("Была выброшена ошибка BusinessException в методе testCustomControllerAdvice");
        }
        return new Response("Все ОК");
    }
}
