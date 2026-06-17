package com.study.app.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncodeTest {
	
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encodedPassword = encoder.encode("admin");

        System.out.println(encodedPassword);
    }

}
