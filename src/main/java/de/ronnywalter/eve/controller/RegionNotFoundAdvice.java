package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.exception.RegionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RegionNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(RegionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String employeeNotFoundHandler(RegionNotFoundException ex) {
        return ex.getMessage();
    }
}
