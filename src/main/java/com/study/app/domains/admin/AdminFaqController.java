package com.study.app.domains.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.dto.FaqDTO;
import com.study.app.domains.faq.FaqService;

@RestController
@RequestMapping("/admin/faq")
public class AdminFaqController {
	
	@Autowired
	private FaqService faqService;
	
	@PostMapping("/add")
	public ResponseEntity<Map<String, Object>> addFaq(@RequestBody FaqDTO dto){
		Map<String, Object> response = new HashMap<>();
		
		try {
			faqService.addFaq(dto);
			
			response.put("success", true);
            response.put("message", "FAQ가 등록되었습니다.");
            return ResponseEntity.ok(response);
		}catch(Exception e) {
			e.printStackTrace();
            response.put("success", false);
            response.put("message", "FAQ 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
		}
		
	}
	
	@DeleteMapping("/delete/{faqId}")
	public ResponseEntity<Map<String,Object>> deleteFaq(@PathVariable Long faqId){
		Map<String, Object> response = new HashMap<>();
		try {
			faqService.deleteFaq(faqId);
			response.put("success", true);
            response.put("message", "FAQ가 삭제되었습니다.");
            return ResponseEntity.ok(response);
			
		}catch(Exception e) {
			e.printStackTrace();
            response.put("success", false);
            response.put("message", "FAQ 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
		}
	}
	
	@PutMapping("/update/{faqId}")
	public ResponseEntity<Map<String,Object>> updateFaq(@PathVariable Long faqId, @RequestBody FaqDTO dto){
		dto.setFaq_id(faqId);
		Map<String, Object> response = new HashMap<>();
		try {
			faqService.updateFaq(dto);
			response.put("success", true);
            response.put("message", "FAQ가 수정되었습니다.");
            return ResponseEntity.ok(response);
		}catch(Exception e) {
			e.printStackTrace();
            response.put("success", false);
            response.put("message", "FAQ 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
		}
	}
	
}
