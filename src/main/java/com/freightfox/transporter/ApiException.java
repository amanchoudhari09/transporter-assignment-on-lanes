package com.freightfox.transporter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

class ApiException extends RuntimeException { final int status; final String code; ApiException(int status,String code,String message){super(message);this.status=status;this.code=code;} }
@RestControllerAdvice
class ApiExceptionHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<ErrorResponse> api(ApiException e){return ResponseEntity.status(e.status).body(new ErrorResponse("error",e.code,e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){String msg=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+" "+x.getDefaultMessage()).collect(Collectors.joining(", "));return ResponseEntity.badRequest().body(new ErrorResponse("error","VALIDATION_ERROR",msg));}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> other(Exception e){return ResponseEntity.internalServerError().body(new ErrorResponse("error","INTERNAL_ERROR","An unexpected error occurred."));}
}
