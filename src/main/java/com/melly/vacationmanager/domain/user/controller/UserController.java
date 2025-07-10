package com.melly.vacationmanager.domain.user.controller;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.service.UserService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements ResponseController {
    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<ResponseDto> createUser(@Validated @RequestBody SignUpRequest request) {

        return null;
    }


}
