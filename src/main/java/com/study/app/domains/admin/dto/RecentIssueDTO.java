package com.study.app.domains.admin.dto;

public class RecentIssueDTO {
	
    private String type;
    private String title;
    private String description;
    private String time;
    private String status;
    
    public RecentIssueDTO() {}

	public RecentIssueDTO(String type, String title, String description, String time, String status) {
		super();
		this.type = type;
		this.title = title;
		this.description = description;
		this.time = time;
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    
	

}
