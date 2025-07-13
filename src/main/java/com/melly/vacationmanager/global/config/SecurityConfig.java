package com.melly.vacationmanager.global.config;

import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.auth.CustomAuthenticationProvider;
import com.melly.vacationmanager.global.auth.CustomLoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 커스텀 로그인 필터 생성 및 설정
        CustomLoginFilter loginFilter = new CustomLoginFilter(authenticationManager(authenticationConfiguration));
        loginFilter.setFilterProcessesUrl("/api/v1/auth/login");      // 로그인 처리 URL 변경

        // 메서드 체이닝은 (.) 기준으로 줄 바꿈 사용
        // 람다 식 파라미터가 하나일 때 괄호를 생략 가능
        http
                .csrf(AbstractHttpConfigurer::disable)     // 람다식을 메서드 레퍼런스로 치환
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/signup","/admin/pending").permitAll()
                        .requestMatchers("/api/v1/users","/api/v1/users/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 로그인 안 되어 있는 경우 리다이렉트
                            response.sendRedirect("/");
                        })
                )
                .authenticationProvider(customAuthenticationProvider())
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(AbstractHttpConfigurer::disable);   // 기본 시큐리티 로그아웃 비활성화
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/js/**", "/css/**", "/images/**", "/favicon.ico");
    }

    @Bean   // Spring Security 에서 인증을 처리하는 핵심 객체로, 사용자가 제공한 자격 증명(예: 아이디, 비밀번호)을 기반으로 인증을 수행
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(userRepository, passwordEncoder());
    }
}
