package com.study.app.domains.api;

import java.time.LocalDateTime;

public class TermsDTO {
	
    private Long terms_id;
    private String terms_type;
    private String title;
    private String content;
    private String version;
    private String required_yn;
    private String is_active;
    private LocalDateTime created_at;

    public TermsDTO() {}

	public TermsDTO(Long terms_id, String terms_type, String title, String content, String version, String required_yn,
			String is_active, LocalDateTime created_at) {
		super();
		this.terms_id = terms_id;
		this.terms_type = terms_type;
		this.title = title;
		this.content = content;
		this.version = version;
		this.required_yn = required_yn;
		this.is_active = is_active;
		this.created_at = created_at;
	}

	public Long getTerms_id() {
		return terms_id;
	}

	public void setTerms_id(Long terms_id) {
		this.terms_id = terms_id;
	}

	public String getTerms_type() {
		return terms_type;
	}

	public void setTerms_type(String terms_type) {
		this.terms_type = terms_type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRequired_yn() {
		return required_yn;
	}

	public void setRequired_yn(String required_yn) {
		this.required_yn = required_yn;
	}

	public String getIs_active() {
		return is_active;
	}

	public void setIs_active(String is_active) {
		this.is_active = is_active;
	}

	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}

    
    

}
