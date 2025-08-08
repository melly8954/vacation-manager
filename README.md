# Vacation Manager

## 프로젝트 개요
**Vacation Manager**는 개인 학습 목적으로 JPA, QueryDSL, 공통 모듈 설계 등 백엔드 핵심 기술을 익히고 실무에 적용하기 위해 개발한 휴가 관리 시스템입니다. 실제 업무 프로세스를 모방하여 회원 관리, 휴가 신청, 승인, 잔여일 관리, 통계 등 주요 기능을 구현함으로써 기술 숙련도와 설계 역량을 향상하는 데 중점을 두었습니다.
([프로젝트 개요 및 상세 설계서] https://northern-mongoose-47b.notion.site/Vacation-Manager-219d351413c08057ba63e362a575b135)  

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
- RESTful API 설계 및 표준 응답 포맷 적용 ([API 설계서](https://northern-mongoose-47b.notion.site/API-220d351413c0802cbf21fd91cd480324))  
- ERD 기반 데이터베이스 설계 ([ERD 설계서](https://northern-mongoose-47b.notion.site/ERD-21ed351413c0804e82f4f34b148ccfac))
- Spring Security를 활용한 세션 기반 인증 및 권한 관리
- QueryDSL을 사용해 안전하고 확장 가능한 동적 쿼리 구현
- 파일 저장 경로 분리  
- 예외 처리와 일관된 오류 응답 체계 구축

## 확장성 및 자동화
- `@Scheduled` 기반의 배치 작업으로 매년 자동 휴가 지급 기능을 구현하여 운영 효율성을 높였습니다.
- 휴가 신청 및 상태 변경 내역을 `VacationAuditLog`에 기록하여 변경 이력 추적 및 감사 기능 제공

## 테스트 및 로깅
- JUnit 5 기반 단위 테스트 코드 작성으로 안정적인 기능 검증 수행
- p6spy를 활용해 DB 쿼리 로깅을 구현하여 쿼리 성능 모니터링 및 문제점 파악

## UI/UX  
- FullCalendar.js를 활용한 휴가 일정 직관적 시각화  
- Bootstrap과 jQuery 기반 반응형 UI 구현  
- 모달 창을 통한 휴가 상세 내역 및 증빙 파일 확인
