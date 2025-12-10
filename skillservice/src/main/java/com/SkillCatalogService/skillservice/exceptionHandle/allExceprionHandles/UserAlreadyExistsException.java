package com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
