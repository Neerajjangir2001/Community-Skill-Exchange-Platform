package com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles;

public class UserNotFound extends RuntimeException {

    public UserNotFound(String message) {
        super(message);
    }
}
