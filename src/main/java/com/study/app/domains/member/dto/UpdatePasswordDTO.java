package com.study.app.domains.member.dto;

public class UpdatePasswordDTO {
	
	private String member_id;
	private String current_password;
	private String new_password;
	public String getMember_id() {
		return member_id;
	}
	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}
	public String getCurrent_password() {
		return current_password;
	}
	public void setCurrent_password(String current_password) {
		this.current_password = current_password;
	}
	public String getNew_password() {
		return new_password;
	}
	public void setNew_password(String new_password) {
		this.new_password = new_password;
	}
	public UpdatePasswordDTO(String member_id, String current_password, String new_password) {
		super();
		this.member_id = member_id;
		this.current_password = current_password;
		this.new_password = new_password;
	}
	
	public UpdatePasswordDTO() {}
	
}
