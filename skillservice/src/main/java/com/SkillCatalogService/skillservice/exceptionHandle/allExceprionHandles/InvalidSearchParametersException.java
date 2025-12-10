package com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles;

public class InvalidSearchParametersException extends RuntimeException {
    public InvalidSearchParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}