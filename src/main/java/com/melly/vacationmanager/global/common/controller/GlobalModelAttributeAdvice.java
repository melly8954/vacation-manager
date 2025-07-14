package com.melly.vacationmanager.global.common.controller;

import com.melly.vacationmanager.domain.user.dto.response.UserInfoResponse;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.global.auth.PrincipalDetails;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {
    private final IUserService userService;

    @ModelAttribute("login_user")
    public UserInfoResponse addLoginUserToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("요청 시 SecurityContext 인증 정보: {}", authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof PrincipalDetails principalDetails) {
                Long userId = principalDetails.getUserEntity().getUserId();
                return userService.getUserInfo(userId);
            }
        }
        return null;
    }
}
