package com.study.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.dto.MemberDTO;
import com.study.app.dto.LoginDTO; // Import LoginDTO
import com.study.app.services.MemberService;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/signup")
    public String signup(@RequestBody MemberDTO memberDTO) {
        int result = memberService.signup(memberDTO);
        if (result > 0) {
            return "success";
        } else {
            return "fail";
        }
    }
    

}
