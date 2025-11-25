package com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles;

import java.util.NoSuchElementException;

public class ProfileNotFoundException extends NoSuchElementException {

    @Override
    public String getMessage() {
        return "Profile not found";
    }

}
