package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class PostReportDTO {
	
    private Long report_id;
    private Long post_id;
    private String member_id;
    private String reason;
    private LocalDate created_at;

    public PostReportDTO() {}

	public PostReportDTO(Long report_id, Long post_id, String member_id, String reason, LocalDate created_at) {
		super();
		this.report_id = report_id;
		this.post_id = post_id;
		this.member_id = member_id;
		this.reason = reason;
		this.created_at = created_at;
	}

	public Long getReport_id() {
		return report_id;
	}

	public void setReport_id(Long report_id) {
		this.report_id = report_id;
	}

	public Long getPost_id() {
		return post_id;
	}

	public void setPost_id(Long post_id) {
		this.post_id = post_id;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
    
    

}
