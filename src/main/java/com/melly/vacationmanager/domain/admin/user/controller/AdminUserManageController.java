package com.melly.vacationmanager.domain.admin.user.controller;

import com.melly.vacationmanager.domain.admin.user.dto.ProcessStatusRequest;
import com.melly.vacationmanager.domain.admin.user.dto.AdminUserManagePendingPageResponse;
import com.melly.vacationmanager.domain.admin.user.dto.UserStatusChangeResponse;
import com.melly.vacationmanager.domain.admin.user.service.IAdminUserManageService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserManageController implements ResponseController {

    private final IAdminUserManageService adminUserManageService;

    @GetMapping("/pending")
    public ResponseEntity<ResponseDto> findPendingUsers(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "ALL") String  year,
                                                        @RequestParam(defaultValue = "ALL") String  month,
                                                        @RequestParam(defaultValue = "") String name,
                                                        @RequestParam(defaultValue = "desc") String order){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        //  정렬 기준을 설정
        Sort sortBy = Sort.by(Sort.Order.by("userId"));
        //  정렬 방향을 설정
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        AdminUserManagePendingPageResponse response = adminUserManageService.findPendingUsers(year, month, name, pageable);

        return makeResponseEntity(HttpStatus.OK,"null","승인 대기 사용자 목록 조회 성공",response);
    }

    @PatchMapping("{userId}/status")
    public ResponseEntity<ResponseDto> processPendingUsers(@PathVariable Long userId,
                                                           @RequestBody ProcessStatusRequest request){
        UserStatusChangeResponse response = adminUserManageService.processPendingUsers(userId, request);
        return makeResponseEntity(HttpStatus.OK, null, "사용자 상태가 성공적으로 변경되었습니다.", response);
    }
}
