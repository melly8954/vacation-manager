# Vacation Manager

## 프로젝트 개요  
사내 휴가 신청부터 승인, 잔여일 관리, 통계 기능까지 구현한 풀스택 휴가 관리 시스템입니다.

## 사용 기술 스택  
- **Frontend:** HTML/CSS/JavaScript, jQuery, Bootstrap, FullCalendar.js, Mustache  
- **Backend:** Java 17, Spring Boot, Spring Data JPA, Spring Security, Hibernate Validator, Apache POI  
- **Database:** MySQL  
- **기타:** Gradle, Postman, Git, IntelliJ IDEA

## 주요 기능  
- 회원 가입 및 관리자 승인 프로세스  
- 휴가 신청, 승인, 취소 기능  
- 휴가 잔여일 실시간 조회  
- 증빙 파일 업로드 및 다운로드  
- FullCalendar 기반 휴가 캘린더 시각화  
- 관리자 페이지 (가입 승인 대기, 휴가 통계 등)

## 설계 및 아키텍처  
- RESTful API 설계 및 표준 응답 포맷 적용  
- ERD 기반 데이터베이스 설계  
- Spring Security를 활용한 세션 기반 인증 및 권한 관리  
- 파일 저장 경로 분리 및 보안 처리  
- 예외 처리와 일관된 오류 응답 체계 구축

## 확장성 및 품질 관리  
- Hibernate Validator를 통한 요청 파라미터 유효성 검증  
- `@Scheduled` 배치 작업으로 자동 연차 지급 기능 구현  
- 향후 MSA 전환을 고려한 모듈화 및 서비스 단위 분리 설계  
- 테스트 코드 작성 및 자동화 배포(있을 경우)

## UI/UX  
- FullCalendar.js를 활용한 휴가 일정 직관적 시각화  
- Bootstrap과 jQuery 기반 반응형 UI 구현  
- 모달 창을 통한 휴가 상세 내역 및 증빙 파일 확인
