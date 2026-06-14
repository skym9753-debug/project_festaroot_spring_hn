package com.study.app.domains.member.dto;

public class FindIdRequestDTO {

    private String name;
    private String email;
    
    public FindIdRequestDTO() {}

	public FindIdRequestDTO(String name, String email) {
		super();
		this.name = name;
		this.email = email;
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
