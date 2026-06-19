package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class CommentReportDTO {
	
    private Long report_id;
    private Long comment_id;
    private String member_id;
    private String reason;
    private LocalDate created_at;
    
    public CommentReportDTO() {}

	public CommentReportDTO(Long report_id, Long comment_id, String member_id, String reason, LocalDate created_at) {
		super();
		this.report_id = report_id;
		this.comment_id = comment_id;
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

	public Long getComment_id() {
		return comment_id;
	}

	public void setComment_id(Long comment_id) {
		this.comment_id = comment_id;
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
