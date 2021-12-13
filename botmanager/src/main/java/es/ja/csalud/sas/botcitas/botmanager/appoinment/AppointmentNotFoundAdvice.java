package es.ja.csalud.sas.botcitas.botmanager.appoinment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class AppointmentNotFoundAdvice {

  @ResponseBody
  @ExceptionHandler(AppointmentNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String employeeNotFoundHandler(AppointmentNotFoundException ex) {
    return ex.getMessage();
  }
}