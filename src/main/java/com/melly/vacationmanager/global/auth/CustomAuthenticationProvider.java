package com.melly.vacationmanager.global.auth;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder 등

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        // 1. 사용자 조회
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 계정 상태 체크 (예: PENDING, REJECTED 등)
        if (user.getStatus() == UserStatus.PENDING) {
            throw new DisabledException("PENDING");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new DisabledException("REJECTED");
        }

        // 4. 인증 토큰 생성 (권한 정보 등 추가 가능)
        List<GrantedAuthority> authorities = List.of(/* 권한 추가 */);

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}