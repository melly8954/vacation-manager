package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;

public interface IUserService {
    void signUp(SignUpRequest request);
    void duplicateCheck(String type, String value);
}
