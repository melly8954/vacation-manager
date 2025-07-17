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

    @GetMapping("/admin/pending")
    public String pending() {
        return "admin/pending";
    }
}
