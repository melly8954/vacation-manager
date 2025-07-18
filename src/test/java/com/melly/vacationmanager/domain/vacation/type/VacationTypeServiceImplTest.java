package com.melly.vacationmanager.domain.vacation.type;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.domain.vacation.type.service.VacationTypeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VacationTypeServiceImplTest {
    @Mock
    private VacationTypeRepository vacationTypeRepository;

    @InjectMocks
    private VacationTypeServiceImpl vacationTypeService;

    @Test
    @DisplayName("정상 흐름 - 휴가 타입 전체 조회")
    void getAllTypes_returnsAllVacationTypes() {
        // given
        List<VacationTypeEntity> mockEntities = Arrays.asList(
                VacationTypeEntity.builder()
                        .typeCode("ANNUAL")
                        .typeName("연차")
                        .build(),
                VacationTypeEntity.builder()
                        .typeCode("SICK")
                        .typeName("병가")
                        .build()
        );
        when(vacationTypeRepository.findAll()).thenReturn(mockEntities);

        // when
        VacationTypeListResponse response = vacationTypeService.getAllTypes();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypes()).hasSize(2);
        assertThat(response.getTypes())
                .extracting(VacationTypeDto::getTypeCode)
                .containsExactly("ANNUAL", "SICK");
    }

    @Test
    @DisplayName("정상 흐름 - 휴가 타입을 코드로 조회")
    void findByTypeCode_returnsEntityIfExists() {
        // given
        VacationTypeEntity entity = VacationTypeEntity.builder()
                .typeCode("ANNUAL")
                .typeName("연차")
                .build();
        when(vacationTypeRepository.findByTypeCode("ANNUAL")).thenReturn(Optional.of(entity));

        // when
        Optional<VacationTypeEntity> result = vacationTypeService.findByTypeCode("ANNUAL");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTypeCode()).isEqualTo("ANNUAL");
        assertThat(result.get().getTypeName()).isEqualTo("연차");
    }
}
