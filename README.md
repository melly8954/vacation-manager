# Vacation Manager

## 프로젝트 개요
**Vacation Manager**는 개인 학습 목적으로 JPA, QueryDSL, 공통 모듈 설계 등 백엔드 핵심 기술을 익히고 실무에 적용하기 위해 개발한 휴가 관리 시스템입니다. 실제 업무 프로세스를 모방하여 회원 관리, 휴가 신청, 승인, 잔여일 관리, 통계 등 주요 기능을 구현함으로써 기술 숙련도와 설계 역량을 향상하는 데 중점을 두었습니다. 

([프로젝트 개요 및 상세 설계서](https://northern-mongoose-47b.notion.site/Vacation-Manager-219d351413c08057ba63e362a575b135)) <br><br><br>

## 사용 기술 스택  
- **Frontend:** HTML/CSS/JavaScript, jQuery, Bootstrap, FullCalendar.js, Mustache  
- **Backend:** Java 17, Spring Boot, Spring Data JPA, QueryDSL, Spring Security
- **Database:** MySQL  
- **기타:** Gradle, Postman, Git, IntelliJ IDEA <br><br><br>

## 주요 기능  
### 사용자
- 사용자 가입 및 로그인 프로세스
  <img width="1914" height="887" alt="Image" src="https://github.com/user-attachments/assets/e1ba9df0-5d55-41f3-b7d5-9ad145952ec2" /><br><br><br><br><br> 
- 휴가 신청 및 취소
  <img width="1912" height="893" alt="Image" src="https://github.com/user-attachments/assets/60ec468f-5653-4a20-a53f-a5ea8d2ea1e5" />
  <img width="1904" height="888" alt="Image" src="https://github.com/user-attachments/assets/29b7f841-07a1-4fee-8bf3-99fcd00767ae" /><br><br><br><br><br> 
- 휴가 잔여일 실시간 조회
  <img width="1909" height="871" alt="Image" src="https://github.com/user-attachments/assets/6439a552-4707-4e25-a17f-4bc788f09019" /><br><br><br><br><br>      
- FullCalendar 기반 휴가 캘린더 시각화
  <img width="1902" height="885" alt="Image" src="https://github.com/user-attachments/assets/16ec107f-e14f-4677-8158-831cc268ca4c" /><br><br><br><br><br> 
- 증빙 파일 업로드 및 다운로드<br><br><br><br><br> 

### 관리자  
- 사용자 가입 승인 처리
  <img width="1907" height="884" alt="Image" src="https://github.com/user-attachments/assets/c9468d8b-cb13-4f33-b9ad-3ad1415a39dd" /><br><br><br><br><br> 
- 휴가 신청 내역 승인 및 상태 변경
  <img width="1914" height="888" alt="Image" src="https://github.com/user-attachments/assets/e237a070-8a55-4366-bf1d-e4be2e50b8ce" />
  <img width="1918" height="570" alt="Image" src="https://github.com/user-attachments/assets/28f339ed-7a78-47e3-b17a-4f1a0b6c4b60" /><br><br><br><br><br>  
- 휴가 사용 통계 조회 및 분석
  <img width="1903" height="861" alt="Image" src="https://github.com/user-attachments/assets/2374765b-106a-4d24-a2f3-7ed6bdef68bf" />
  <img width="1911" height="756" alt="Image" src="https://github.com/user-attachments/assets/f953bdd0-f3c6-4afc-8b6b-14d7143f220a" />
  <img width="1236" height="529" alt="Image" src="https://github.com/user-attachments/assets/aa72f8c5-1319-4ae0-be3b-55b8a217c81e" /><br><br><br><br><br>  

## 설계 및 아키텍처  
- RESTful API 설계 및 표준 응답 포맷 적용 ([API 설계서](https://northern-mongoose-47b.notion.site/API-220d351413c0802cbf21fd91cd480324))  
- ERD 기반 데이터베이스 설계 ([ERD 설계서](https://northern-mongoose-47b.notion.site/ERD-21ed351413c0804e82f4f34b148ccfac))
- Spring Security를 활용한 세션 기반 인증 및 권한 관리
- QueryDSL을 사용해 안전하고 확장 가능한 동적 쿼리 구현
- 파일 저장 경로 분리  
- 예외 처리와 일관된 오류 응답 체계 구축<br><br><br>

## 확장성 및 자동화
- `@Scheduled` 기반의 배치 작업으로 매년 자동 휴가 지급 기능을 구현하여 운영 효율성을 높였습니다.
  <img width="1127" height="567" alt="Image" src="https://github.com/user-attachments/assets/c4b1ae99-28d0-4780-a56d-c23327434f6a" /><br><br>
- 휴가 신청 및 상태 변경 내역을 `VacationAuditLog`에 기록하여 변경 이력 추적 및 감사 기능 제공<br><br><br>

## 테스트 및 로깅
- JUnit 5 기반 단위 테스트 코드를 작성하여 안정적인 기능 검증을 수행하고, <br>
  Mockito를 활용해 의존성 객체를 가짜(mock)로 대체함으로써 외부 영향 없이 독립적인 테스트가 가능하도록 구현하였습니다.
  <img width="1150" height="555" alt="Image" src="https://github.com/user-attachments/assets/52db881d-2868-476d-8d3e-44d2718ee9e1" /><br><br>
- p6spy를 활용해 DB 쿼리 로깅을 구현하여 쿼리 성능 모니터링 및 문제점 파악
  <img width="1220" height="658" alt="Image" src="https://github.com/user-attachments/assets/33b5c6a8-18c7-4677-b55d-e67813309138" />
  <img width="1245" height="572" alt="Image" src="https://github.com/user-attachments/assets/793c682c-d202-4bad-855d-bc7294cc0b64" /> <br><br><br>

## UI/UX  
- FullCalendar.js를 활용한 휴가 일정 직관적 시각화  
- Bootstrap과 jQuery 기반 반응형 UI 구현  
- 모달 창을 통한 휴가 상세 내역 및 증빙 파일 확인




