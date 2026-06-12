package com.study.app.domains.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.inquiry.dto.InquiryDTO;

@RestController
@RequestMapping("/inquiry")
public class InquiryController {
	
	@Autowired
	private InquiryService inqServ;
	
	/**
	 * 1:1 문의 등록 엔드포인트
	 * multipart/form-data 요청을 @ModelAttribute로 받음
	 */
	@PostMapping("/add")
	public ResponseEntity<String> addInquiry(@ModelAttribute InquiryDTO dto) {
		try {
			inqServ.addInquiry(dto);
			return ResponseEntity.ok("문의가 성공적으로 등록되었습니다.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("문의 등록 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
	@GetMapping("/list/{memberId}")
	public ResponseEntity<List<InquiryDTO>> inquiryList(@PathVariable String memberId){
		List<InquiryDTO> list = inqServ.getMyInquiryList(memberId);
		return ResponseEntity.ok(list);
	}
	
	@GetMapping("/detail/{inquiryId}")
	public ResponseEntity<InquiryDTO> inquiryDetail(@PathVariable Long inquiryId){
		InquiryDTO dto = inqServ.inquiryDetail(inquiryId);
		return ResponseEntity.ok(dto);
	}
	
}
