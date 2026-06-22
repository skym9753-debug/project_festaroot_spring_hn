package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class PostReportDTO {
	
    private Long report_id;
    private Long post_id;
    private String member_id;
    private String reason;
    private LocalDate created_at;
    
    
    // POST_REPORT.STATUS
    private String status;

    // 관리자 화면용 접수 일시
    private String created_at_text;

    // MEMBER_REPORT_HISTORY 조회 결과
    private String admin_memo;
    private String processed_at;

    // 관리자 신고 접수 목록용
    private String post_title;
    private String post_category;
    private Integer post_report_count;
    
    private String author;

    public PostReportDTO() {}

	public PostReportDTO(Long report_id, Long post_id, String member_id, String reason, LocalDate created_at,
			String status, String created_at_text, String admin_memo, String processed_at, String post_title,
			String post_category, Integer post_report_count, String author) {
		super();
		this.report_id = report_id;
		this.post_id = post_id;
		this.member_id = member_id;
		this.reason = reason;
		this.created_at = created_at;
		this.status = status;
		this.created_at_text = created_at_text;
		this.admin_memo = admin_memo;
		this.processed_at = processed_at;
		this.post_title = post_title;
		this.post_category = post_category;
		this.post_report_count = post_report_count;
		this.author = author;
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

	public String getPost_title() {
		return post_title;
	}

	public void setPost_title(String post_title) {
		this.post_title = post_title;
	}

	public String getPost_category() {
		return post_category;
	}

	public void setPost_category(String post_category) {
		this.post_category = post_category;
	}

	public Integer getPost_report_count() {
		return post_report_count;
	}

	public void setPost_report_count(Integer post_report_count) {
		this.post_report_count = post_report_count;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}


    

}
