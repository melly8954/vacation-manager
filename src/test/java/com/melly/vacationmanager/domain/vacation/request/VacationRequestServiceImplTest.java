package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.domain.filestorage.service.IEvidenceFileService;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.repository.VacationAuditLogRepository;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.response.EvidenceFileListResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.EvidenceFileResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VRCancelResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.request.service.VacationRequestServiceImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import com.melly.vacationmanager.global.common.utils.CurrentUserUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacationRequestServiceImplTest {
    @Mock private VacationRequestRepository vacationRequestRepository;
    @Mock private IUserService userService;
    @Mock private IVacationTypeService vacationTypeService;
    @Mock private IVacationBalanceService vacationBalanceService;
    @Mock private EvidenceFileRepository evidenceFileRepository;
    @Mock private IEvidenceFileService evidenceFileService;
    @Mock private VacationAuditLogRepository vacationAuditLogRepository;

    @InjectMocks
    private VacationRequestServiceImpl vacationRequestService;

    @Nested
    @DisplayName("휴가 신청 정상 흐름 테스트")
    class RequestVacationTests {
        @Test
        @DisplayName("정상 흐름 - 증빙 파일 없이 휴가 요청 저장")
        void success_withoutEvidenceFiles(){
            // given
            Long userId = 1L;
            String typeCode = "ANNUAL";
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(2);
            BigDecimal daysCount = new BigDecimal("3");

            VacationRequestDto requestDto = VacationRequestDto.builder()
                    .typeCode(typeCode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .daysCount(daysCount)
                    .reason("휴가 신청 테스트")
                    .build();

            UserEntity user = mock(UserEntity.class);
            VacationTypeEntity vacationType = mock(VacationTypeEntity.class);
            VacationBalanceEntity balance = mock(VacationBalanceEntity.class);

            when(userService.findByUserId(userId)).thenReturn(Optional.of(user));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(vacationType));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.of(balance));
            when(balance.getRemainingDays()).thenReturn(daysCount);
            when(vacationRequestRepository.existsApprovedOverlap(userId, startDate, endDate)).thenReturn(false);
            when(vacationRequestRepository.save(any(VacationRequestEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            vacationRequestService.requestVacation(requestDto, Collections.emptyList(), userId);

            // then
            verify(vacationRequestRepository, times(1)).save(any(VacationRequestEntity.class));
            verify(evidenceFileRepository, never()).save(any());
        }

        @Test
        @DisplayName("정상 흐름 - 증빙 파일 포함 휴가 요청 저장")
        void success_withEvidenceFiles() throws Exception {
            // given
            Long userId = 1L;
            String typeCode = "ANNUAL";
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(2);
            BigDecimal daysCount = new BigDecimal("3");

            VacationRequestDto requestDto = VacationRequestDto.builder()
                    .typeCode(typeCode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .daysCount(daysCount)
                    .reason("휴가 신청 테스트 - 파일 포함")
                    .build();

            UserEntity user = mock(UserEntity.class);
            VacationTypeEntity vacationType = mock(VacationTypeEntity.class);
            VacationBalanceEntity balance = mock(VacationBalanceEntity.class);

            when(userService.findByUserId(userId)).thenReturn(Optional.of(user));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(vacationType));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.of(balance));
            when(balance.getRemainingDays()).thenReturn(daysCount);
            when(vacationRequestRepository.existsApprovedOverlap(userId, startDate, endDate)).thenReturn(false);
            when(vacationRequestRepository.save(any(VacationRequestEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // 증빙파일 준비 - Mock MultipartFile 2개
            MultipartFile file1 = mock(MultipartFile.class);
            when(file1.isEmpty()).thenReturn(false);
            when(file1.getOriginalFilename()).thenReturn("file1.pdf");
            when(file1.getBytes()).thenReturn("파일내용1".getBytes());
            when(file1.getSize()).thenReturn(100L);
            when(file1.getContentType()).thenReturn("application/pdf");

            MultipartFile file2 = mock(MultipartFile.class);
            when(file2.isEmpty()).thenReturn(false);
            when(file2.getOriginalFilename()).thenReturn("file2.jpg");
            when(file2.getBytes()).thenReturn("파일내용2".getBytes());
            when(file2.getSize()).thenReturn(200L);
            when(file2.getContentType()).thenReturn("image/jpeg");

            List<MultipartFile> evidenceFiles = List.of(file1, file2);

            // evidenceFileService.saveEvidenceFile(...) 호출 시 가짜 URL 반환
            when(evidenceFileService.saveEvidenceFile(anyString(), any(byte[].class)))
                    .thenReturn("http://localhost:8081/images/evidence_files/fakefile1.pdf")
                    .thenReturn("http://localhost:8081/images/evidence_files/fakefile2.jpg");

            // when
            vacationRequestService.requestVacation(requestDto, evidenceFiles, userId);

            // then
            verify(vacationRequestRepository, times(1)).save(any(VacationRequestEntity.class));
            verify(evidenceFileRepository, times(2)).save(any(EvidenceFileEntity.class));

            // 파일 저장 서비스 호출 횟수 확인
            verify(evidenceFileService, times(2)).saveEvidenceFile(anyString(), any(byte[].class));
        }
    }

    @Nested
    @DisplayName("휴가 신청 예외 흐름 테스트")
    class RequestVacationExceptionTests {
        Long userId = 1L;
        String typeCode = "ANNUAL";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(1);
        BigDecimal daysCount = BigDecimal.ONE;

        VacationRequestDto baseDto = VacationRequestDto.builder()
                .typeCode(typeCode)
                .startDate(startDate)
                .endDate(endDate)
                .daysCount(daysCount)
                .reason("예외 테스트")
                .build();

        @Test
        @DisplayName("예외 흐름 - 사용자 없음")
        void fail_userNotFound() {
            when(userService.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 흐름 - 휴가 타입 없음")
        void fail_vacationTypeNotFound() {
            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VACATION_TYPE_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 흐름 - 날짜 범위 없음")
        void fail_invalidDateRange() {
            VacationRequestDto dto = VacationRequestDto.builder()
                    .typeCode(typeCode)
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(1)) // startDate > endDate
                    .daysCount(daysCount)
                    .build();

            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));

            assertThatThrownBy(() -> vacationRequestService.requestVacation(dto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_DATE_RANGE);
        }

        @Test
        @DisplayName("예외 흐름 - 휴가 일수 음수")
        void fail_invalidDaysCount() {
            VacationRequestDto dto = VacationRequestDto.builder()
                    .typeCode(typeCode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .daysCount(new BigDecimal("-1"))
                    .build();

            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));

            assertThatThrownBy(() -> vacationRequestService.requestVacation(dto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_DAYS_COUNT);
        }

        @Test
        @DisplayName("예외 흐름 - 잔여 휴가 없음")
        void fail_vacationBalanceNotFound() {
            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.empty());

            VacationRequestDto dto = baseDto;

            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VACATION_BALANCE_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 흐름 - 잔여 휴가 부족")
        void fail_insufficientBalance() {
            VacationBalanceEntity balance = mock(VacationBalanceEntity.class);
            when(balance.getRemainingDays()).thenReturn(BigDecimal.ZERO);

            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.of(balance));

            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("예외 흐름 - 승인된 휴가 중복")
        void fail_overlappingApprovedVacation() {
            VacationBalanceEntity balance = mock(VacationBalanceEntity.class);
            when(balance.getRemainingDays()).thenReturn(daysCount);

            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.of(balance));
            when(vacationRequestRepository.existsApprovedOverlap(userId, startDate, endDate)).thenReturn(true);

            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, Collections.emptyList(), userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.OVERLAPPING_APPROVED_VACATION);
        }

        @Test
        @DisplayName("예외 흐름 - 증빙 파일 저장 실패")
        void fail_fileUploadFailed() throws Exception {
            VacationBalanceEntity balance = mock(VacationBalanceEntity.class);
            when(balance.getRemainingDays()).thenReturn(daysCount);

            when(userService.findByUserId(userId)).thenReturn(Optional.of(mock(UserEntity.class)));
            when(vacationTypeService.findByTypeCode(typeCode)).thenReturn(Optional.of(mock(VacationTypeEntity.class)));
            when(vacationBalanceService.findById(new VacationBalanceId(userId, typeCode))).thenReturn(Optional.of(balance));
            when(vacationRequestRepository.existsApprovedOverlap(userId, startDate, endDate)).thenReturn(false);

            VacationRequestDto dto = baseDto;

            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("failfile.pdf");
            when(file.getBytes()).thenThrow(new IOException("강제 IOException 발생"));

            List<MultipartFile> evidenceFiles = List.of(file);

            CustomException ex = assertThrows(CustomException.class, () -> {
                vacationRequestService.requestVacation(dto, evidenceFiles, userId);
            });


            assertThatThrownBy(() -> vacationRequestService.requestVacation(baseDto, evidenceFiles, userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Nested
    @DisplayName("getEvidenceFiles 메서드 테스트")
    class GetEvidenceFiles {
        @Test
        @DisplayName("정상 흐름 - 첨부 파일이 존재하는 경우 매핑된 리스트 반환")
        void success_filesExist() {
            // given
            Long requestId = 1L;
            when(vacationRequestRepository.existsByRequestId(requestId)).thenReturn(true);

            EvidenceFileEntity file1 = createEvidenceFileEntity("file1.pdf", "uuid1");
            EvidenceFileEntity file2 = createEvidenceFileEntity("file2.png", "uuid2");
            when(evidenceFileRepository.findAllByVacationRequest_RequestId(requestId))
                    .thenReturn(List.of(file1, file2));

            // when
            EvidenceFileListResponse result = vacationRequestService.getEvidenceFiles(requestId);


            // then
            assertThat(result.getEvidenceFiles()).hasSize(2);
            assertThat(result.getEvidenceFiles().get(0).getOriginalName()).isEqualTo("file1.pdf");
            assertThat(result.getEvidenceFiles().get(1).getOriginalName()).isEqualTo("file2.png");
        }

        @Test
        @DisplayName("정상 흐름 - 첨부 파일이 없는 경우 빈 리스트 반환")
        void success_noFiles() {
            // given
            Long requestId = 2L;
            when(vacationRequestRepository.existsByRequestId(requestId)).thenReturn(true);
            when(evidenceFileRepository.findAllByVacationRequest_RequestId(requestId))
                    .thenReturn(Collections.emptyList());

            // when
            EvidenceFileListResponse result = vacationRequestService.getEvidenceFiles(requestId);

            // then
            assertThat(result.getEvidenceFiles()).isEmpty();
        }

        @Test
        @DisplayName("예외 흐름 - 휴가 요청 ID가 존재하지 않으면 예외 발생")
        void fail_vacationRequestNotFound() {
            // given
            Long invalidRequestId = 999L;
            when(vacationRequestRepository.existsByRequestId(invalidRequestId)).thenReturn(false);

            // expect
            assertThatThrownBy(() -> vacationRequestService.getEvidenceFiles(invalidRequestId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VACATION_REQUEST_NOT_FOUND);
        }

        private EvidenceFileEntity createEvidenceFileEntity(String originalName, String uuid) {
            return EvidenceFileEntity.builder()
                    .originalName(originalName)
                    .uniqueName(uuid)
                    .savedPath("/upload/" + uuid)
                    .fileSize(1024L)
                    .fileType("application/pdf")
                    .uploadedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Nested
    @DisplayName("cancelVacationRequest 메서드 테스트")
    class cancelVacationRequest {
        @Mock private UserEntity mockUser; // 변경자 유저 객체 모킹

        @Test
        @DisplayName("정상 흐름 - 상태 변경 및 감사 로그 저장")
        void success_cancelVacationRequest() {
            // given
            Long requestId = 1L;
            VacationRequestEntity entity = VacationRequestEntity.builder()
                    .requestId(requestId)
                    .status(VacationRequestStatus.PENDING)
                    .build();

            when(vacationRequestRepository.findByRequestId(requestId))
                    .thenReturn(Optional.of(entity));

            try (MockedStatic<CurrentUserUtils> mockedCurrentUserUtils = Mockito.mockStatic(CurrentUserUtils.class)) {
                mockedCurrentUserUtils.when(CurrentUserUtils::getUser).thenReturn(mockUser);
                mockedCurrentUserUtils.when(CurrentUserUtils::getRole).thenReturn("USER");

                // when
                VRCancelResponse response = vacationRequestService.cancelVacationRequest(requestId);

                // then
                assertThat(entity.getStatus()).isEqualTo(VacationRequestStatus.CANCELED);

                assertThat(response.getRequestId()).isEqualTo(requestId);
                assertThat(response.getNewStatus()).isEqualTo(VacationRequestStatus.CANCELED.name());

                ArgumentCaptor<VacationAuditLogEntity> captor = ArgumentCaptor.forClass(VacationAuditLogEntity.class);
                verify(vacationAuditLogRepository).save(captor.capture());

                VacationAuditLogEntity savedLog = captor.getValue();
                assertThat(savedLog.getRequest()).isSameAs(entity);
                assertThat(savedLog.getChangedBy()).isSameAs(mockUser);
                assertThat(savedLog.getChangedByRole()).isEqualTo("USER");
                assertThat(savedLog.getOldStatus()).isEqualTo("PENDING");
                assertThat(savedLog.getNewStatus()).isEqualTo("CANCELED");
                assertThat(savedLog.getComment()).isEqualTo("사용자 직접 취소");
            }
        }

        @Test
        @DisplayName("예외 흐름 - 존재하지 않는 요청 ID로 예외 발생")
        void fail_cancelVacationRequest_notFound() {
            Long invalidRequestId = 999L;
            when(vacationRequestRepository.findByRequestId(invalidRequestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vacationRequestService.cancelVacationRequest(invalidRequestId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VACATION_REQUEST_NOT_FOUND);
        }
    }
}
