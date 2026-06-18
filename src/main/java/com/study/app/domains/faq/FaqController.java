package com.study.app.domains.faq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.dto.FaqDTO;

@RestController
@RequestMapping("/faq")
public class FaqController {
	
	@Autowired
	private FaqService faqService;
	
	@GetMapping("/list")
	public ResponseEntity<List<FaqDTO>> getFaqList(){
		List<FaqDTO> list = faqService.getFaqList();
		return ResponseEntity.ok(list);
		
	}
}
