package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class Example6Controller {

    @GetMapping(value = "/testResponseStatusException", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testResponseStatusException(@RequestParam(required = false, defaultValue = "false") boolean exception) {
        if (exception){
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "ResponseStatusException in testResponseStatusException");
        }
        return new Response("Все ОК!!!");
    }
}
