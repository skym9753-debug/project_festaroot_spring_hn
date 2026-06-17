package com.study.app.domains.admin.dto;

import java.util.List;

public class AdminDashboardDTO {
	
    // 상단 요약 통계
    private AdminSummaryDTO summary;

    // 최근 7일 통계
    private List<WeeklyStatDTO> weeklyStats;

    // 축제 상태별 통계
    private List<FestivalStatusStatDTO> festivalStatusStats;

    // 지역별 축제 통계
    private List<RegionStatDTO> regionStats;

    // 인기 축제 목록
    private List<PopularFestivalDTO> popularFestivals;

    // 최근 신고 목록
    private List<RecentReportDTO> recentReports;

    // 최근 운영 이슈 목록
    private List<RecentIssueDTO> recentIssues;
    
    public AdminDashboardDTO() {}

	public AdminDashboardDTO(AdminSummaryDTO summary, List<WeeklyStatDTO> weeklyStats,
			List<FestivalStatusStatDTO> festivalStatusStats, List<RegionStatDTO> regionStats,
			List<PopularFestivalDTO> popularFestivals, List<RecentReportDTO> recentReports,
			List<RecentIssueDTO> recentIssues) {
		super();
		this.summary = summary;
		this.weeklyStats = weeklyStats;
		this.festivalStatusStats = festivalStatusStats;
		this.regionStats = regionStats;
		this.popularFestivals = popularFestivals;
		this.recentReports = recentReports;
		this.recentIssues = recentIssues;
	}

	public AdminSummaryDTO getSummary() {
		return summary;
	}

	public void setSummary(AdminSummaryDTO summary) {
		this.summary = summary;
	}

	public List<WeeklyStatDTO> getWeeklyStats() {
		return weeklyStats;
	}

	public void setWeeklyStats(List<WeeklyStatDTO> weeklyStats) {
		this.weeklyStats = weeklyStats;
	}

	public List<FestivalStatusStatDTO> getFestivalStatusStats() {
		return festivalStatusStats;
	}

	public void setFestivalStatusStats(List<FestivalStatusStatDTO> festivalStatusStats) {
		this.festivalStatusStats = festivalStatusStats;
	}

	public List<RegionStatDTO> getRegionStats() {
		return regionStats;
	}

	public void setRegionStats(List<RegionStatDTO> regionStats) {
		this.regionStats = regionStats;
	}

	public List<PopularFestivalDTO> getPopularFestivals() {
		return popularFestivals;
	}

	public void setPopularFestivals(List<PopularFestivalDTO> popularFestivals) {
		this.popularFestivals = popularFestivals;
	}

	public List<RecentReportDTO> getRecentReports() {
		return recentReports;
	}

	public void setRecentReports(List<RecentReportDTO> recentReports) {
		this.recentReports = recentReports;
	}

	public List<RecentIssueDTO> getRecentIssues() {
		return recentIssues;
	}

	public void setRecentIssues(List<RecentIssueDTO> recentIssues) {
		this.recentIssues = recentIssues;
	}
    
    
    

}
