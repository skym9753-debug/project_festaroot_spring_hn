package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class CommentReportDTO {
	
    private Long report_id;
    private Long comment_id;
    private String member_id;
    private String reason;
    private LocalDate created_at;
    
    // COMMENT_REPORT.STATUS
    private String status;

    // 관리자 화면 날짜 표시용
    private String created_at_text;

    // MEMBER_REPORT_HISTORY 조회값
    private String admin_memo;
    private String processed_at;

    
    public CommentReportDTO() {}


	public CommentReportDTO(Long report_id, Long comment_id, String member_id, String reason, LocalDate created_at,
			String status, String created_at_text, String admin_memo, String processed_at) {
		super();
		this.report_id = report_id;
		this.comment_id = comment_id;
		this.member_id = member_id;
		this.reason = reason;
		this.created_at = created_at;
		this.status = status;
		this.created_at_text = created_at_text;
		this.admin_memo = admin_memo;
		this.processed_at = processed_at;
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


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getCreated_at_text() {
		return created_at_text;
	}


	public void setCreated_at_text(String created_at_text) {
		this.created_at_text = created_at_text;
	}


	public String getAdmin_memo() {
		return admin_memo;
	}


	public void setAdmin_memo(String admin_memo) {
		this.admin_memo = admin_memo;
	}


	public String getProcessed_at() {
		return processed_at;
	}


	public void setProcessed_at(String processed_at) {
		this.processed_at = processed_at;
	}



}
