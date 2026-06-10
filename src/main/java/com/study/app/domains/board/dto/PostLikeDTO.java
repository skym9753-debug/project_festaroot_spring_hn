package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class PostLikeDTO {
	
    private String member_id;
    private Long post_id;
    private LocalDate created_at;

    public PostLikeDTO() {}

	public PostLikeDTO(String member_id, Long post_id, LocalDate created_at) {
		super();
		this.member_id = member_id;
		this.post_id = post_id;
		this.created_at = created_at;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public Long getPost_id() {
		return post_id;
	}

	public void setPost_id(Long post_id) {
		this.post_id = post_id;
	}

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
    
    
    
    

}
