package io.kamenskiy.situations.exception_handling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR) //будет возвращаться статус-код 500
public class ServiceException extends Exception{
    public ServiceException() {
    }
    public ServiceException(String message) {
        super(message);
    }
}
