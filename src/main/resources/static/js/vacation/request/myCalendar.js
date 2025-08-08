$(document).ready(function() {
    // FullCalendar 초기화
    var calendarEl = document.getElementById('calendar');
    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: '' // 명시적으로 비움
        },
        events: function(fetchInfo, successCallback, failureCallback) {
            $.ajax({
                url: '/api/v1/vacation-requests/me/calendar',
                method: 'GET',
                data: {
                    startDate: fetchInfo.startStr.substring(0, 10), // "2025-07-27"
                    endDate: fetchInfo.endStr.substring(0, 10)      // "2025-08-02"
                },
                success: function(response) {
                    // 백엔드에서 받은 response.data 배열을 FullCalendar 이벤트 포맷으로 변환
                    let events = response.data.vacationEvents.map(item => ({
                        id: item.requestId,
                        title: item.typeName + (item.daysCount === 0.5 ? ' [반차] ' : ''),
                        start: item.startDate,
                        end: addOneDay(item.endDate),
                        color: '#28a745',
                        extendedProps: {
                            status: item.status,
                            daysCount: item.daysCount
                        }
                    }));
                    successCallback(events);
                },
                error: function() {
                    failureCallback();
                }
            });
        },
        eventClick: function(info) {
            const startDateStr = info.event.startStr;
            const endDateStr = info.event.endStr ? subtractOneDay(info.event.endStr) : startDateStr;

            alert(
                '휴가 종류: ' + info.event.title + '\n' +
                '상태: ' + info.event.extendedProps.status + '\n' +
                '휴가 기간: ' + info.event.extendedProps.daysCount + '일\n' +
                '기간: ' + startDateStr + ' ~ ' + endDateStr
            );
        },
        dayCellDidMount: function(info) {
            const todayStr = new Date().toISOString().substring(0, 10);
            if (info.dateStr === todayStr) {
                // 오늘 날짜 배경색 변경 (예: 밝은 노란색)
                info.el.style.backgroundColor = '#fff3cd'; // Bootstrap warning-light 계열
                info.el.style.borderRadius = '5px';
            }
        }
    });

    calendar.render();
});

function addOneDay(dateStr) {
    let date = new Date(dateStr);
    date.setDate(date.getDate() + 1);
    return date.toISOString().substring(0, 10);
}

function subtractOneDay(dateStr) {
    let date = new Date(dateStr);
    date.setDate(date.getDate() - 1);
    return date.toISOString().substring(0, 10);
}