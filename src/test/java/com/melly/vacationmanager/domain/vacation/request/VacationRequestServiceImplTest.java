package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.domain.filestorage.service.IEvidenceFileService;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.request.service.VacationRequestServiceImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private VacationRequestServiceImpl vacationRequestService;

    @Nested
    @DisplayName("휴가 신청 테스트")
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
}
