package com.study.app.domains.admin.dto;

public class AdminSummaryDTO {
	
    private int memberCount;
    private int todayNewMembers;
    private int memberChange;

    private int festivalCount;
    private int visibleFestivalCount;
    private int festivalChange;

    private int postCount;
    private int todayPostCount;
    private int postChange;

    private int reportCount;
    private int waitingReportCount;
    private int reportChange;

    private int waitingInquiryCount;
    private int todayInquiryCount;
    private int inquiryChange;

    private int noticeCount;
    private int visibleNoticeCount;
    private int noticeChange;
    
    public AdminSummaryDTO() {}

	public AdminSummaryDTO(int memberCount, int todayNewMembers, int memberChange, int festivalCount,
			int visibleFestivalCount, int festivalChange, int postCount, int todayPostCount, int postChange,
			int reportCount, int waitingReportCount, int reportChange, int waitingInquiryCount, int todayInquiryCount,
			int inquiryChange, int noticeCount, int visibleNoticeCount, int noticeChange) {
		super();
		this.memberCount = memberCount;
		this.todayNewMembers = todayNewMembers;
		this.memberChange = memberChange;
		this.festivalCount = festivalCount;
		this.visibleFestivalCount = visibleFestivalCount;
		this.festivalChange = festivalChange;
		this.postCount = postCount;
		this.todayPostCount = todayPostCount;
		this.postChange = postChange;
		this.reportCount = reportCount;
		this.waitingReportCount = waitingReportCount;
		this.reportChange = reportChange;
		this.waitingInquiryCount = waitingInquiryCount;
		this.todayInquiryCount = todayInquiryCount;
		this.inquiryChange = inquiryChange;
		this.noticeCount = noticeCount;
		this.visibleNoticeCount = visibleNoticeCount;
		this.noticeChange = noticeChange;
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

	public int getMemberChange() {
		return memberChange;
	}

	public void setMemberChange(int memberChange) {
		this.memberChange = memberChange;
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

	public int getFestivalChange() {
		return festivalChange;
	}

	public void setFestivalChange(int festivalChange) {
		this.festivalChange = festivalChange;
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

	public int getPostChange() {
		return postChange;
	}

	public void setPostChange(int postChange) {
		this.postChange = postChange;
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

	public int getReportChange() {
		return reportChange;
	}

	public void setReportChange(int reportChange) {
		this.reportChange = reportChange;
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

	public int getInquiryChange() {
		return inquiryChange;
	}

	public void setInquiryChange(int inquiryChange) {
		this.inquiryChange = inquiryChange;
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

	public int getNoticeChange() {
		return noticeChange;
	}

	public void setNoticeChange(int noticeChange) {
		this.noticeChange = noticeChange;
	}

    


}
