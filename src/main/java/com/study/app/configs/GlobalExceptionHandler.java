package com.study.app.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handleExpired(
            TokenExpiredException e
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("JWT 토큰이 만료되었습니다.");
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<String> handleJwt(
            JWTVerificationException e
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("유효하지 않은 토큰입니다.");
    }

}
