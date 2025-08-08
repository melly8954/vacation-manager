# Vacation Manager

## 프로젝트 개요  
사내 휴가 신청부터 승인, 잔여일 관리, 통계 기능까지 구현한 풀스택 휴가 관리 시스템입니다.

## 사용 기술 스택  
- **Frontend:** HTML/CSS/JavaScript, jQuery, Bootstrap, FullCalendar.js, Mustache  
- **Backend:** Java 17, Spring Boot, Spring Data JPA, QueryDSL, Spring Security
- **Database:** MySQL  
- **기타:** Gradle, Postman, Git, IntelliJ IDEA

## 주요 기능  
### 사용자
- 사용자 가입 및 로그인 프로세스
- 휴가 신청 및 취소  
- 휴가 잔여일 실시간 조회  
- 증빙 파일 업로드 및 다운로드  
- FullCalendar 기반 휴가 캘린더 시각화  

### 관리자  
- 사용자 가입 승인 처리 
- 휴가 신청 내역 승인 및 상태 변경  
- 휴가 사용 통계 조회 및 분석

## 설계 및 아키텍처  
- RESTful API 설계 및 표준 응답 포맷 적용  
- ERD 기반 데이터베이스 설계  
- Spring Security를 활용한 세션 기반 인증 및 권한 관리
- QueryDSL을 사용해 안전하고 확장 가능한 동적 쿼리 구현
- 파일 저장 경로 분리  
- 예외 처리와 일관된 오류 응답 체계 구축

## 확장성 및 자동화
- `@Scheduled` 기반의 배치 작업으로 매년 자동 휴가 지급 기능을 구현하여 운영 효율성을 높였습니다.

## UI/UX  
- FullCalendar.js를 활용한 휴가 일정 직관적 시각화  
- Bootstrap과 jQuery 기반 반응형 UI 구현  
- 모달 창을 통한 휴가 상세 내역 및 증빙 파일 확인
