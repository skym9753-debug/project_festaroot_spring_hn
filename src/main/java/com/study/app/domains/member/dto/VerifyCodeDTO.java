package com.study.app.domains.member.dto;

public class VerifyCodeDTO {
	
    private String email;
    private String verificationCode;
    
    public VerifyCodeDTO() {}

	public VerifyCodeDTO(String email, String verificationCode) {
		super();
		this.email = email;
		this.verificationCode = verificationCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
    
    

}
