package com.study.app.domains.member.dto;

public class PasswordFindRequestDTO {

    private String member_id;
    private String name;
    private String email;
    
    public PasswordFindRequestDTO() {}

	public PasswordFindRequestDTO(String member_id, String name, String email) {
		super();
		this.member_id = member_id;
		this.name = name;
		this.email = email;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
    
}
