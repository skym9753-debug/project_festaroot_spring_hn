package com.study.app.domains.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

	@Autowired
    private EmailService emailService;


    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendCode(
            @RequestBody Map<String, String> req) {

        String email = req.get("email");

        emailService.sendVerificationCode(email);
        System.out.println(req);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "인증번호가 발송되었습니다.");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestBody Map<String, String> req) {

        String email = req.get("email");
        String code = req.get("code");

        boolean verified = emailService.verifyCode(email, code);

        Map<String, Object> result = new HashMap<>();
        result.put("success", verified);

        if (verified) {
            result.put("message", "이메일 인증이 완료되었습니다.");
        } else {
            result.put("message", "인증번호가 일치하지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
