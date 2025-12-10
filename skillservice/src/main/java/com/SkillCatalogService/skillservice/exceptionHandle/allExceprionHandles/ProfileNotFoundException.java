package com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles;

import org.springframework.aot.hint.RuntimeHints;

import java.util.NoSuchElementException;

public class ProfileNotFoundException extends RuntimeException {


    public  ProfileNotFoundException(String message) {
       super(message);
    }

}
