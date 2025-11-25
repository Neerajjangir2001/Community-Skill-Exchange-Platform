package com.UserProfileService.userprofile.exceptionHandle;


import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.ProfileNotFoundException;
import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.UserAlreadyExistsException;
import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.UserNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFound exception){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public  ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistsException exception){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public  ResponseEntity<ErrorResponse> profileNotFound(ProfileNotFoundException exception){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


}
