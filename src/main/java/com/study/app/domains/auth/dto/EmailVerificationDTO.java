package com.study.app.domains.auth.dto;

import java.time.LocalDateTime;

public class EmailVerificationDTO {
	
    private String email;
    private String code;
    private String verified;
    private LocalDateTime expires_at;
    private LocalDateTime created_at;

    public EmailVerificationDTO() {}

	public EmailVerificationDTO(String email, String code, String verified, LocalDateTime expires_at,
			LocalDateTime created_at) {
		super();
		this.email = email;
		this.code = code;
		this.verified = verified;
		this.expires_at = expires_at;
		this.created_at = created_at;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getVerified() {
		return verified;
	}

	public void setVerified(String verified) {
		this.verified = verified;
	}

	public LocalDateTime getExpires_at() {
		return expires_at;
	}

	public void setExpires_at(LocalDateTime expires_at) {
		this.expires_at = expires_at;
	}

	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}



}
