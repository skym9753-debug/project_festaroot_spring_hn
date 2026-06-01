package com.study.app.domains.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.auth.AuthService;


@RestController
@RequestMapping("/oauth")
public class OauthController {
	
	@Autowired
	public AuthService authService;
	
	
	@GetMapping("/kakao/callback")
	public ResponseEntity<Map<String, Object>> kakaoCallback(@RequestParam String code) {

	    Map<String, Object> result = authService.kakaoLogin(code);
	   
	    return ResponseEntity.ok(result);
	}
	
	@GetMapping("/naver/callback")
	public ResponseEntity<Map<String, Object>> naverLogin(@RequestParam String code, @RequestParam String state) {

		Map<String, Object> result = authService.naverLogin(code, state);
		
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/google/callback")
	public ResponseEntity<Map<String, Object>> googleLogin(
	        @RequestParam String code) {

	    return ResponseEntity.ok(
	        authService.googleLogin(code)
	    );
	}
}
