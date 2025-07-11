package com.melly.vacationmanager.domain.user.dto.request;

import com.melly.vacationmanager.global.common.enums.UserPosition;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank(message = "username은 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "username은 8자 이상 20자 이하로 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "username은 영문, 숫자만 허용합니다.")
    private String username;

    @NotBlank(message = "password는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "password는 8자 이상 20자 이하로 입력하세요.")
    @Pattern(
            regexp = "^(?![!@#$%^&*])[a-z0-9!@#$%^&*]+$",
            message = "password는 소문자, 숫자, 특수문자를 포함해야 하며 특수문자로 시작할 수 없습니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하로 입력하세요.")
    private String name;

    @NotBlank(message = "email은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식을 입력하세요.")
    @Size(max = 50, message = "email은 50자 이하로 입력하세요.")
    private String email;

    @NotBlank(message = "입사일은 필수 입력 항목입니다.")
    private LocalDate hiredate; // LocalDate로 받을 수도 있으나, 문자열로 받아서 파싱 처리 가능

    @NotNull(message = "직급은 필수 선택 항목입니다.")
    private UserPosition position;
}