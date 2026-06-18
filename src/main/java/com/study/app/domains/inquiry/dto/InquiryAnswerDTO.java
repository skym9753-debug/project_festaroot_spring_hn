package com.study.app.domains.inquiry.dto;

import java.time.LocalDate;

public class InquiryAnswerDTO {
	
	private Long answer_id;
	private Long inquiry_id;
	private String admin_id;
	private String content;
	private LocalDate created_at;
	
	public InquiryAnswerDTO() {}
	
	public InquiryAnswerDTO(Long answer_id, Long inquiry_id, String admin_id, String content, LocalDate created_at) {
		super();
		this.answer_id = answer_id;
		this.inquiry_id = inquiry_id;
		this.admin_id = admin_id;
		this.content = content;
		this.created_at = created_at;
	}
	public Long getAnswer_id() {
		return answer_id;
	}
	public void setAnswer_id(Long answer_id) {
		this.answer_id = answer_id;
	}
	public Long getInquiry_id() {
		return inquiry_id;
	}
	public void setInquiry_id(Long inquiry_id) {
		this.inquiry_id = inquiry_id;
	}
	public String getAdmin_id() {
		return admin_id;
	}
	public void setAdmin_id(String admin_id) {
		this.admin_id = admin_id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalDate getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
	
	
	
	
}
