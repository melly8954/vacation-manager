package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.dto.response.UserInfoResponse;
import com.melly.vacationmanager.domain.user.entity.UserEntity;

import java.util.Optional;

public interface IUserService {
    void signUp(SignUpRequest request);
    void duplicateCheck(String type, String value);

    UserInfoResponse getUserInfo(Long userId);

    Optional<UserEntity> findByUserId(Long userId);

}
