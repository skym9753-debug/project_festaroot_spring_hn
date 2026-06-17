package com.study.app.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.auth.AuthService;
import com.study.app.domains.auth.dto.LoginDTO;
import com.study.app.utils.JWTUtil;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	public AuthService authService;
	
	@Autowired
	public JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody LoginDTO loginDTO) {

        Map<String, Object> result = new HashMap<>();

        String token = authService.login(loginDTO);

        if (token == null) {
            result.put("success", false);
            result.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        String role = jwtUtil.getRole(token);

        if (!"admin".equals(role)) {
            result.put("success", false);
            result.put("message", "관리자만 접근할 수 있습니다.");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        result.put("success", true);
        result.put("message", loginDTO.getMember_id() + " 관리자님, 환영합니다!");
        result.put("token", token);
        result.put("role", role);

        return ResponseEntity.ok(result);
    }

}

