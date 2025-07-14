package com.melly.vacationmanager.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.auth.dto.request.LoginRequest;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
// UsernamePasswordAuthenticationFilter --> 로그인 요청을 처리하고, 인증 토큰을 생성하여 인증 관리
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 요청 바디에서 username, password 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            log.info("로그인 시도 - 아이디: {}", username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 JSON 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                            final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        // super.successfulAuthentication(request, response, chain, authResult); 를 맨 앞에 호출하면 수동 등록할 필요는 없음
        // 인증된 Authentication 객체를 SecurityContext에 수동으로 등록 --> 직접 JSON 응답을 커스터마이징하고 있다면, 수동 등록
        SecurityContextHolder.getContext().setAuthentication(authResult);

        // 인증 객체가 제대로 등록됐는지 로그로 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext에 등록된 Authentication: {}", authentication);
        log.info("Authentication principal: {}", authentication.getPrincipal());
        log.info("Authentication authenticated: {}", authentication.isAuthenticated());

        // 세션에 사용자 ID 저장
        HttpSession session = request.getSession(true);
        Object principal = authResult.getPrincipal();

        if (principal instanceof PrincipalDetails principalDetails) {
            session.setAttribute("LOGIN_USER_ID", principalDetails.getUserEntity().getUserId());
        }

        // SecurityContext 세션 저장
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        // 응답 데이터 준비
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        // 예: 로그인 성공 사용자명 JSON 응답
        String username = authResult.getName();
        String json = "{\"message\":\"로그인 성공\", \"username\":\"" + username + "\"}";

        response.getWriter().write(json);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String errorCode = "auth_failed";
        String message = "로그인에 실패하였습니다.";

        if (failed instanceof BadCredentialsException) {
            errorCode = "bad_credentials";
            message = "비밀번호가 일치하지 않습니다.";

        } else if (failed instanceof DisabledException) {
            // isEnabled() == false 에서 발생하므로, 여기서 상태값 구분해야 함
            if (failed.getMessage().contains("PENDING")) {
                status = HttpServletResponse.SC_FORBIDDEN;
                errorCode = "user_pending";
                message = "관리자 승인 대기 중입니다.";
            } else {
                status = HttpServletResponse.SC_FORBIDDEN;
                errorCode = "user_rejected";
                message = "승인이 거절된 사용자입니다.";
            }
        }

        // JSON 응답 구성
        ResponseDto responseDto = new ResponseDto(status, errorCode, message, null);

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        // Jackson 으로 객체 → JSON 직렬화
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), responseDto);

        log.debug("로그인 실패 - {}: {}", errorCode, failed.getMessage());
    }
}
