package br.com.exceptions.handler;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.exceptions.BadRequestException;
import br.com.exceptions.StandardError;
import br.com.exceptions.UserNotAuthenticatedException;

@RestController
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler{
	
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<StandardError> handleAllExceptions(Exception ex, WebRequest request){
		StandardError error = new StandardError(ex.getMessage(),request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(UserNotAuthenticatedException.class)
	@ResponseBody
	public final ResponseEntity<StandardError> handleUserNotAuthenticatedException(UserNotAuthenticatedException ex, WebRequest request){
		StandardError error = new StandardError(ex.getMessage(),request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public final ResponseEntity<StandardError> handleAccessDeniedException(AccessDeniedException ex, WebRequest request){
		StandardError error = new StandardError(ex.getMessage(),request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<StandardError> handleBadRequestException(BadRequestException ex, WebRequest request){
		StandardError error = new StandardError(ex.getMessage(),request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(DateTimeParseException.class)
	public final ResponseEntity<StandardError> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
		StandardError error = new StandardError("Invalid date format! " + ex.getMessage(), request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String errorMessage = ex.getAllErrors()
                .stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
		
		StandardError error = new StandardError(errorMessage,request.getDescription(false), Instant.now());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	
	}	
}
