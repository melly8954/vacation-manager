$(document).ready(function() {
    const vacationType = $('#vacationType');
    const halfDayOption = $('#halfDayOption');
    const isHalfDay = $('#isHalfDay');
    const startDate = $('#startDate');
    const endDate = $('#endDate');
    const daysCount = $('#daysCount');

    // 휴가 타입 목록 조회
    $.getJSON('/api/v1/vacation-types')
        .done(function(response) {
            vacationType.append('<option value="">-- 선택 --</option>');
            response.data.forEach(function(type) {
                vacationType.append(`<option value="${type.typeCode}">${type.typeName}</option>`);
            });
        })
        .fail(function() {
            alert('휴가 유형 로딩 실패');
        });

    // 연가 선택 시 반차 선택여부 추가
    vacationType.on('change', function() {
        if (vacationType.val() === 'ANNUAL') {
            halfDayOption.show();
        } else {
            halfDayOption.hide();
            isHalfDay.prop('checked', false);
            endDate.prop('readonly', false);
            updateDaysCount();
        }
    });

    // 반차 선택 시 시작일/종료일 처리
    function updateDateInputsForHalfDay(isHalfDay) {
        if (isHalfDay) {
            endDate.val(startDate.val());
            endDate.prop('readonly', true);
        } else {
            endDate.prop('readonly', false);
        }
    }

    // 반차 체크박스 변경 이벤트
    isHalfDay.on('change', function() {
        const isChecked = isHalfDay.is(':checked');
        updateDateInputsForHalfDay(isChecked);
        updateDaysCount();
    });

    // 시작일 변경 시 종료일도 자동 조정 (반차일 때만)
    startDate.on('change', function() {
        if (isHalfDay.is(':checked')) {
            endDate.val(startDate.val());
        }
        updateDaysCount();
    });

    // 종료일 변경 시 (반차 아닐 때만)
    endDate.on('change', function() {
        if (!isHalfDay.is(':checked')) {
            updateDaysCount();
        }
    });

    // 휴가 일수 계산 함수
    function updateDaysCount() {
        const start = startDate.val();
        const end = endDate.val();
        const isHalfDayChecked = isHalfDay.is(':checked');
        if (!start || !end) {
            daysCount.val('');
            return;
        }

        if (isHalfDayChecked) {
            daysCount.val('0.5');
        } else {
            const startDt = new Date(start);
            const endDt = new Date(end);
            const diffTime = endDt - startDt;
            const diffDays = diffTime / (1000 * 60 * 60 * 24) + 1;

            if (diffDays > 0) {
                daysCount.val(diffDays);
            } else {
                daysCount.val('');
            }
        }
    }
});
