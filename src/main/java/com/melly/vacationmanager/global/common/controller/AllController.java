package com.melly.vacationmanager.global.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AllController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signup")
    public String signup() {
        return "user/signup";
    }

    @GetMapping("/vacation-request/apply")
    public String showVacationApplyForm() {
        return "vacation/request/apply";
    }

    @GetMapping("/vacation-request/me")
    public String showVacationMyList() {
        return "vacation/request/myList";
    }

    @GetMapping("/vacation-request/me/calendar")
    public String showVacationMyCalendar() {
        return "vacation/request/myCalendar";
    }

    @GetMapping("/vacation-balance/me")
    public String showVacationMyBalance() {
        return "vacation/balance/myBalance";
    }

    @GetMapping("/admin/pending")
    public String pending() {
        return "admin/pending";
    }

    @GetMapping("/admin/vacation-request/list")
    public String list() {
        return "admin/vacation/request/list";
    }

    @GetMapping("/admin/vacation-statistics")
    public String statistics() {
        return "admin/vacation/statistics/statistics";
    }
}
