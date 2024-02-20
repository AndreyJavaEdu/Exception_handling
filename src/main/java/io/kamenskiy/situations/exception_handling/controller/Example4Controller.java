package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example4Controller {

    @GetMapping(value = "/testDefaultControllerAdvice", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testDefaultControllerAdvice(@RequestParam(required = false, defaultValue = "false") boolean exception)
        throws BusinessException{
        if(exception){
            throw new  BusinessException("BusinessException in testDefaultControllerAdvice");
        }
        return new Response("All Ok");
    }
}
