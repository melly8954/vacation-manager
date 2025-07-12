package com.melly.vacationmanager.global.common.controller;

import com.melly.vacationmanager.domain.user.dto.response.UserInfoResponse;
import com.melly.vacationmanager.domain.user.service.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {
    private final IUserService userService;

    @ModelAttribute("login_user")
    public UserInfoResponse addLoginUserToModel(HttpSession session) {
        // 세션에서 사용자 ID를 꺼내고
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        if (userId != null) {
            // UserService 통해 유저 정보 조회 후 반환
            return userService.getUserInfo(userId); // 반환값이 Mustache에서 {{login_user.name}}처럼 사용됨
        }
        return null;
    }
}
