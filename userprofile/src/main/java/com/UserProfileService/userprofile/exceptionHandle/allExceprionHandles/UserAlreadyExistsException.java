package com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
