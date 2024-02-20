package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.ServiceException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example2Controller {
@GetMapping(value = "/testResponseStatusExceptionResolver", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testResponseStatusExceptionResolver(@RequestParam(required = false,  defaultValue = "true") boolean exception)
        throws ServiceException{
        if (exception){
            throw new ServiceException("ServiceException in method testResponseStatusExceptionResolver");
        }
        return new Response("Ok");
    }
}
