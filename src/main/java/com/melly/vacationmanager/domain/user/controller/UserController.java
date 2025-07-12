package com.melly.vacationmanager.domain.user.controller;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements ResponseController {
    private final IUserService userService;

    // 사용자 가입 API
    @PostMapping("")
    public ResponseEntity<ResponseDto> createUser(@Validated @RequestBody SignUpRequest request) {
        userService.signUp(request);
        return makeResponseEntity(HttpStatus.CREATED,null,"사용자 가입에 성공했습니다.",null);
    }

    // 필드 중복 검사 API
    @GetMapping("/duplicate-check")
    public ResponseEntity<ResponseDto> duplicateCheck(@RequestParam String type, @RequestParam String value) {
        userService.duplicateCheck(type,value);
        return makeResponseEntity(HttpStatus.OK,null,"중복 검사 통과",null);
    }
}
