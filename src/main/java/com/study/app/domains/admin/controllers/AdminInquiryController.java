package com.study.app.domains.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.inquiry.InquiryService;

@RestController
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {
	
	@Autowired
	private InquiryService inquiryService;
	
}
