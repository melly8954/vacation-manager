package com.melly.vacationmanager.global.common.utils;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.auth.PrincipalDetails;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.exception.CustomException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class CurrentUserUtils {

    public static PrincipalDetails getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED); // or return null
        }
        return (PrincipalDetails) authentication.getPrincipal();
    }

    public static UserEntity getUser() {
        return getCurrentPrincipal().getUserEntity();
    }

    public static Long getUserId() {
        return getCurrentPrincipal().getUserEntity().getUserId();
    }

    public static String getRole() {
        return getCurrentPrincipal().getUserEntity().getRole().name();
    }
}
