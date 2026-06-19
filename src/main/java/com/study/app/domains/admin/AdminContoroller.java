package com.study.app.domains.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.dto.AdminDashboardDTO;
import com.study.app.domains.admin.service.AdminDashboardService;

@RestController
@RequestMapping("/admin")
public class AdminContoroller {
	
    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public AdminDashboardDTO getDashboard(
            @RequestParam(value = "baseDate", required = false) String baseDate
    ) {
        return adminDashboardService.getDashboard(baseDate);
    }
}
