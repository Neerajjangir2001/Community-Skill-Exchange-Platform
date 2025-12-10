package com.UserProfileService.userprofile.securioty;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurityService {
    public boolean isOwner(Authentication authentication, String userId) {

        if (authentication == null || userId == null) {
            return false;
        }

        String authenticatedUserId = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userId.equals(authenticatedUserId);
    }
}
