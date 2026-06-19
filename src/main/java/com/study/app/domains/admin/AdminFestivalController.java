package com.study.app.domains.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.admin.service.AdminFestivalService;
import com.study.app.domains.festival.dto.FestivalDTO;

@RestController
@RequestMapping("/admin/festivals")
public class AdminFestivalController {
	
	@Autowired
	private AdminFestivalService adminFestivalService;
	
	@GetMapping("/list")
	public ResponseEntity<List<FestivalDTO>> getAllFestivals(){
		List<FestivalDTO> list = adminFestivalService.getAllFestivals();
		return ResponseEntity.ok(list);
	}
	
	@PatchMapping("/{contentId}/visibility")
	public ResponseEntity<Map<String, Object>> updateFestivalVisibility(
			@PathVariable Long contentId, 
			@RequestBody Map<String, String> request){
		
		Map<String, Object> response = new HashMap<>();
		String isVisible = request.get("isVisible");
		
		try {
			adminFestivalService.updateVisibility(contentId, isVisible);
			response.put("success", true);
			response.put("message", "공개 여부가 수정되었습니다.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			response.put("success", false);
			response.put("message", "수정 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}
}
