package com.authService.exceptionHandler;


import com.authService.DTO.signup.SignupRequest;
import com.authService.exceptionHandler.allExceprionHandles.PasswordHandleException;
import com.authService.exceptionHandler.allExceprionHandles.RefreshTokenErrorHandles;
import com.authService.exceptionHandler.allExceprionHandles.RegisterSuccess;
import com.authService.exceptionHandler.allExceprionHandles.UserAlreadyExistsException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException exception){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public  ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistsException exception){
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegisterSuccess.class)
    public ResponseEntity<ErrorResponse> handleException(RegisterSuccess exception){
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.OK.value(), exception.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler(PasswordHandleException.class)
    public ResponseEntity<ErrorResponse> passwordHandleException(PasswordHandleException exception){
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Extract the field name (e.g., "password") and the error message
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RefreshTokenErrorHandles.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExceptionHandler(RefreshTokenErrorHandles ex) {

            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}


