package com.melly.vacationmanager.domain.vacation.type.controller;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacation-types")
public class VacationTypeController implements ResponseController {
    private final IVacationTypeService vacationTypeService;

    @GetMapping("")
    public ResponseEntity<ResponseDto> getAllVacationTypes() {
        List<VacationTypeDto> allTypes = vacationTypeService.getAllTypes();
        return makeResponseEntity(HttpStatus.OK,null,"모든 휴가 타입을 조회했습니다.",allTypes);
    }
}
