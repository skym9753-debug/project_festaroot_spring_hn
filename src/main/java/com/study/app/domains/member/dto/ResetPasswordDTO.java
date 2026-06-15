package com.study.app.domains.member.dto;

public class ResetPasswordDTO {
	
    private String member_id;
    private String email;
    private String resetToken;
    private String newPassword;
    
    public ResetPasswordDTO() {}

	public ResetPasswordDTO(String member_id, String email, String resetToken, String newPassword) {
		super();
		this.member_id = member_id;
		this.email = email;
		this.resetToken = resetToken;
		this.newPassword = newPassword;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


    
    

}
