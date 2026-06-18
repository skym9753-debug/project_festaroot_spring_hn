package com.study.app.domains.admin.dto;

public class RecentReportDTO {
	
    private String id;
    private String type;
    private String target;
    private String reporter;
    private String date;
    private String status;
    
    public RecentReportDTO() {}

	public RecentReportDTO(String id, String type, String target, String reporter, String date, String status) {
		super();
		this.id = id;
		this.type = type;
		this.target = target;
		this.reporter = reporter;
		this.date = date;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    

}
