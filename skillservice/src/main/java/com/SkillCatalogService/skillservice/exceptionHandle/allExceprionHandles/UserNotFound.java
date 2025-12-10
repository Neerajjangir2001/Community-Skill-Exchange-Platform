package com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles;

public class UserNotFound extends RuntimeException {

    public UserNotFound(String message) {
        super(message);
    }
}
