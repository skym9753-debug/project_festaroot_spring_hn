package com.study.app.domains.admin.dto;

public class AdminSummaryDTO {
	
    private int memberCount;
    private int todayNewMembers;

    private int festivalCount;
    private int visibleFestivalCount;

    private int postCount;
    private int todayPostCount;

    private int reportCount;
    private int waitingReportCount;

    private int waitingInquiryCount;
    private int todayInquiryCount;

    private int noticeCount;
    private int visibleNoticeCount;
    
    public AdminSummaryDTO() {}

	public AdminSummaryDTO(int memberCount, int todayNewMembers, int festivalCount, int visibleFestivalCount,
			int postCount, int todayPostCount, int reportCount, int waitingReportCount, int waitingInquiryCount,
			int todayInquiryCount, int noticeCount, int visibleNoticeCount) {
		super();
		this.memberCount = memberCount;
		this.todayNewMembers = todayNewMembers;
		this.festivalCount = festivalCount;
		this.visibleFestivalCount = visibleFestivalCount;
		this.postCount = postCount;
		this.todayPostCount = todayPostCount;
		this.reportCount = reportCount;
		this.waitingReportCount = waitingReportCount;
		this.waitingInquiryCount = waitingInquiryCount;
		this.todayInquiryCount = todayInquiryCount;
		this.noticeCount = noticeCount;
		this.visibleNoticeCount = visibleNoticeCount;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public int getTodayNewMembers() {
		return todayNewMembers;
	}

	public void setTodayNewMembers(int todayNewMembers) {
		this.todayNewMembers = todayNewMembers;
	}

	public int getFestivalCount() {
		return festivalCount;
	}

	public void setFestivalCount(int festivalCount) {
		this.festivalCount = festivalCount;
	}

	public int getVisibleFestivalCount() {
		return visibleFestivalCount;
	}

	public void setVisibleFestivalCount(int visibleFestivalCount) {
		this.visibleFestivalCount = visibleFestivalCount;
	}

	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}

	public int getTodayPostCount() {
		return todayPostCount;
	}

	public void setTodayPostCount(int todayPostCount) {
		this.todayPostCount = todayPostCount;
	}

	public int getReportCount() {
		return reportCount;
	}

	public void setReportCount(int reportCount) {
		this.reportCount = reportCount;
	}

	public int getWaitingReportCount() {
		return waitingReportCount;
	}

	public void setWaitingReportCount(int waitingReportCount) {
		this.waitingReportCount = waitingReportCount;
	}

	public int getWaitingInquiryCount() {
		return waitingInquiryCount;
	}

	public void setWaitingInquiryCount(int waitingInquiryCount) {
		this.waitingInquiryCount = waitingInquiryCount;
	}

	public int getTodayInquiryCount() {
		return todayInquiryCount;
	}

	public void setTodayInquiryCount(int todayInquiryCount) {
		this.todayInquiryCount = todayInquiryCount;
	}

	public int getNoticeCount() {
		return noticeCount;
	}

	public void setNoticeCount(int noticeCount) {
		this.noticeCount = noticeCount;
	}

	public int getVisibleNoticeCount() {
		return visibleNoticeCount;
	}

	public void setVisibleNoticeCount(int visibleNoticeCount) {
		this.visibleNoticeCount = visibleNoticeCount;
	}
    
    

}
