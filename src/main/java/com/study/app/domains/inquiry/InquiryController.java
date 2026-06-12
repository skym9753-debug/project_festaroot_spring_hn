package com.study.app.domains.inquiry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.inquiry.dto.InquiryDTO;

@RestController
@RequestMapping("/inquiry")
public class InquiryController {
	
	@Autowired
	private InquiryService inqServ;
	
	
	@PostMapping
	public ResponseEntity<Void> addInquity(InquiryDTO dto){
		
		return ResponseEntity.ok().build();
	}

}
