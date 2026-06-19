package com.study.app.domains.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.inquiry.InquiryService;
import com.study.app.domains.inquiry.dto.InquiryAnswerDTO;
import com.study.app.domains.inquiry.dto.InquiryDTO;
import com.study.app.utils.JWTUtil;

@RestController
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private JWTUtil jwtUtil;

    /**
     * Authorization 헤더에서 JWT 토큰을 꺼내 관리자 ID 추출
     */
    private String resolveAdminId(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }

        String token = authorization.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            return jwtUtil.getSubject(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    @GetMapping("/list")
	public ResponseEntity<List<InquiryDTO>> getInquiryList(){
		List<InquiryDTO> list = inquiryService.getInquiryList();
		return ResponseEntity.ok(list);
	}
    
    @GetMapping("/detail/{inquiryId}")
	public ResponseEntity<InquiryDTO> inquiryDetail(@PathVariable Long inquiryId){
		InquiryDTO dto = inquiryService.inquiryDetail(inquiryId);
		return ResponseEntity.ok(dto);
	}

    /**
     * [관리자] 문의 답변 등록/수정
     * POST /admin/inquiry/answer/{inquiryId}
     */
    @PostMapping("/answer/{inquiryId}")
    public ResponseEntity<Map<String, Object>> saveInquiryAnswer(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestBody InquiryAnswerDTO answerDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Map<String, Object> response = new HashMap<>();
        
        String adminId = resolveAdminId(authorization);
        if (adminId == null) {
            response.put("success", false);
            response.put("message", "관리자 인증이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            answerDTO.setInquiry_id(inquiryId);
            answerDTO.setAdmin_id(adminId);
            
            inquiryService.saveInquiryAnswer(answerDTO);
            
            response.put("success", true);
            response.put("message", "답변이 등록되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "답변 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PutMapping("/answer/update/{inquiryId}")
    public ResponseEntity<Map<String, Object>> updateAnswer(@PathVariable Long inquiryId,@RequestBody InquiryAnswerDTO dto){
    		dto.setInquiry_id(inquiryId);
    		Map<String, Object> response = new HashMap<>();
    		int result = inquiryService.updateAnswer(dto);
    		if(result>0) {
    			response.put("success", true);
            response.put("message", "답변이 등록되었습니다.");
    		}
    		return ResponseEntity.ok(response);
    }
    
    
}
