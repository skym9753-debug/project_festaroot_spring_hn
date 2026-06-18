package com.study.app.domains.admin.dto;

import java.time.LocalDate;

public class FaqDTO {
	
	private Long faq_id;
	private String category;
	private String question;
	private String answer;
	private Long display_order;
	private LocalDate created_at;
	
	
	public FaqDTO() {}
	
	public FaqDTO(Long faq_id, String category, String question, String answer, Long display_order,
			LocalDate created_at) {
		super();
		this.faq_id = faq_id;
		this.category = category;
		this.question = question;
		this.answer = answer;
		this.display_order = display_order;
		this.created_at = created_at;
	}
	public Long getFaq_id() {
		return faq_id;
	}
	public void setFaq_id(Long faq_id) {
		this.faq_id = faq_id;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public Long getDisplay_order() {
		return display_order;
	}
	public void setDisplay_order(Long display_order) {
		this.display_order = display_order;
	}
	public LocalDate getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
	
	
	
	
}
