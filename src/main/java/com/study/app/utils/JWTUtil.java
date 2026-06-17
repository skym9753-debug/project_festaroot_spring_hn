package com.study.app.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JWTUtil {
	
	@Value("${jwt.expiration}")
	private Long expiration;
	
	private Algorithm alg;
	private JWTVerifier jwt;
	
	public JWTUtil(@Value("${jwt.secret}")String secret) {
		this.alg = Algorithm.HMAC256(secret);
		this.jwt = JWT.require(alg).build();
	}
	// 토큰 생성
	public String createToken(String id, String role) {
		return JWT.create()
				.withSubject(id)
				.withClaim("role", role) // "user" or "admin"
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + expiration))
				.sign(alg);
		
	}
	
	// 토큰 유효성 검사하는 함수
	public DecodedJWT validation(String token) {
		return jwt.verify(token);
	}
	
	// subject 편하게 꺼내는 함수
	public String getSubject(String token) {
		return validation(token).getSubject();
	}
	
	// role 편하게 꺼내는 함수
	public String getRole(String token) {
		return validation(token).getClaim("role").asString();
	}
		
}
