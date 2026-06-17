package com.study.app.domains.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.admin.dao.AdminDashboardDAO;
import com.study.app.domains.admin.dto.AdminDashboardDTO;
import com.study.app.domains.admin.dto.FestivalStatusStatDTO;
import com.study.app.domains.admin.dto.RegionStatDTO;

@Service
public class AdminDashboardService {

	@Autowired
	private AdminDashboardDAO adminDashboardDAO;

	// 관리자 대시보드 전체 데이터 조회
	public AdminDashboardDTO getDashboard() {

		AdminDashboardDTO dto = new AdminDashboardDTO();

		// 상단 핵심 통계
		dto.setSummary(adminDashboardDAO.selectSummary());

		// 최근 7일 운영 통계
		dto.setWeeklyStats(adminDashboardDAO.selectWeeklyStats());

		// 축제 상태 통계 + 퍼센트 계산
		List<FestivalStatusStatDTO> festivalStatusStats =
				adminDashboardDAO.selectFestivalStatusStats();

		applyFestivalStatusPercent(festivalStatusStats);
		dto.setFestivalStatusStats(festivalStatusStats);

		// 지역별 축제 통계 + 퍼센트 계산
		List<RegionStatDTO> regionStats =
				adminDashboardDAO.selectRegionStats();

		applyRegionPercent(regionStats);
		dto.setRegionStats(regionStats);

		// 인기 축제 TOP 5
		dto.setPopularFestivals(adminDashboardDAO.selectPopularFestivals());

		// 최근 신고 접수
		dto.setRecentReports(adminDashboardDAO.selectRecentReports());

		// 최근 운영 이슈
		dto.setRecentIssues(adminDashboardDAO.selectRecentIssues());

		return dto;
	}

	// 축제 상태별 비율 계산
	private void applyFestivalStatusPercent(List<FestivalStatusStatDTO> list) {

		if (list == null || list.isEmpty()) {
			return;
		}

		int total = list.stream()
				.mapToInt(FestivalStatusStatDTO::getCount)
				.sum();

		if (total == 0) {
			return;
		}

		for (FestivalStatusStatDTO item : list) {
			int percent = (int) Math.round((item.getCount() * 100.0) / total);
			item.setPercent(percent);
		}
	}

	// 지역별 막대그래프 비율 계산
	private void applyRegionPercent(List<RegionStatDTO> list) {

		if (list == null || list.isEmpty()) {
			return;
		}

		int max = list.stream()
				.mapToInt(RegionStatDTO::getCount)
				.max()
				.orElse(0);

		if (max == 0) {
			return;
		}

		for (RegionStatDTO item : list) {
			int percent = (int) Math.round((item.getCount() * 100.0) / max);
			item.setPercent(percent);
		}
	}

}
