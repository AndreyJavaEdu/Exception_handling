package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.CustomException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example3Controller {
    @GetMapping(value = "/testCustomExceptionResolver", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testCustomHandlerExceptionResolver(@RequestParam(required = false, defaultValue = "false") boolean exception)
        throws CustomException {
        if(exception){
            throw new CustomException("CustomException in testCustomHandlerExceptionResolver");
        }
        return new Response("Ok");
    }
}
