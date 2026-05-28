package com.study.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.dto.LoginDTO;
import com.study.app.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	public AuthService authService;
	
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        if (token != null) {
            return token; // Return the JWT token on successful login
        } else {
            return "login_fail"; // Return a specific message for login failure
        }
    }

}
