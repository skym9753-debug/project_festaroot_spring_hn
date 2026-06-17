package com.study.app.domains.admin.dto;

public class WeeklyStatDTO {
	
    private String day;
    private int members;
    private int posts;
    private int reports;
    
    public WeeklyStatDTO() {}

	public WeeklyStatDTO(String day, int members, int posts, int reports) {
		super();
		this.day = day;
		this.members = members;
		this.posts = posts;
		this.reports = reports;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getMembers() {
		return members;
	}

	public void setMembers(int members) {
		this.members = members;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getReports() {
		return reports;
	}

	public void setReports(int reports) {
		this.reports = reports;
	}
    
    

}
