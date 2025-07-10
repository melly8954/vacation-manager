package com.melly.vacationmanager.domain.user.controller;

import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ResponseDto> createUser(@RequestBody SignUpRequest request) {

        return
    }


}
