package com.study.app.domains.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.achievement.AchievementService;
import com.study.app.domains.member.dto.FindIdRequestDTO;
import com.study.app.domains.member.dto.MemberDTO;
import com.study.app.domains.member.dto.MemberProfileDTO;
import com.study.app.domains.member.dto.PasswordFindRequestDTO;
import com.study.app.domains.member.dto.ResetPasswordDTO;
import com.study.app.domains.member.dto.UpdatePasswordDTO;
import com.study.app.domains.member.dto.VerifyCodeDTO;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/achievements/{id}")
    public ResponseEntity<Map<String, Object>> getMemberAchievements(@PathVariable("id") String id) {
        Map<String, Object> data = achievementService.getUserAchievementData(id);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/signup")
    public String signup(@RequestBody MemberDTO memberDTO) {
 
        int result = memberService.signup(memberDTO);
        if (result > 0) {
            return "success";
        } else {
            return "fail";
        }
        
        
    }
    
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Object>> checkId(
            @RequestParam String member_id) {

        int count = memberDAO.countByMemberId(member_id);

        Map<String, Object> result = new HashMap<>();
        result.put("available", count == 0);
        result.put("message", count == 0 ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(
            @RequestParam String nickname) {

        int count = memberDAO.countByNickname(nickname);

        Map<String, Object> result = new HashMap<>();
        result.put("available", count == 0);
        result.put("message", count == 0 ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.");

        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestParam String email) {

        boolean exists = memberService.existsByEmail(email);

        Map<String, Object> result = new HashMap<>();

        result.put("isAvailable", !exists);

        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/profile/{id}")
    public ResponseEntity<MemberProfileDTO> getProfile(@PathVariable("id") String id){
    		System.out.println(id);
    		MemberProfileDTO dto = memberService.getProfile(id);
    		return ResponseEntity.ok(dto);
    }
    
    @PutMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable("id") String id, 
            MemberDTO memberDTO,
            @RequestParam(value = "profile_image", required = false) org.springframework.web.multipart.MultipartFile profileImage){
        Map<String, Object> result = memberService.updateProfile(id, memberDTO, profileImage);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/attendance/{id}")
    public ResponseEntity<Map<String, Object>> processAttendance(@PathVariable("id") String id) {
        Map<String, Object> result = memberService.checkAndProcessAttendance(id);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody FindIdRequestDTO dto) {
        String memberId = memberService.findId(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "member_id", memberId
        ));
    }

    @PostMapping("/password/send-code")
    public ResponseEntity<?> sendPasswordResetCode(@RequestBody PasswordFindRequestDTO dto) {
        memberService.sendPasswordResetCode(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "인증번호가 발송되었습니다."
        ));
    }

    @PostMapping("/password/verify-code")
    public ResponseEntity<?> verifyPasswordResetCode(@RequestBody VerifyCodeDTO dto) {
        String resetToken = memberService.verifyPasswordResetCode(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "resetToken", resetToken
        ));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        memberService.resetPassword(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "비밀번호가 변경되었습니다."
        ));
    }
    
    @PutMapping("/password/update")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordDTO dto){
    			
    		memberService.updatePassword(dto);
    		return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdrawMember(@PathVariable("id") String id) {
        memberService.withdrawMember(id);
        return ResponseEntity.ok().build();
    }

}
