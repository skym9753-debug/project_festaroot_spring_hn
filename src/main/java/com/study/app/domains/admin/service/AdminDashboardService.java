package com.study.app.domains.admin.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
	public AdminDashboardDTO getDashboard(String baseDate) {
		String targetDate = normalizeBaseDate(baseDate);

		AdminDashboardDTO dto = new AdminDashboardDTO();

		// 상단 요약 통계는 현재 DB 기준
		dto.setSummary(adminDashboardDAO.selectSummary());

		// 최근 7일 통계만 선택한 기준일 사용
		dto.setWeeklyStats(adminDashboardDAO.selectWeeklyStats(targetDate));

		// 축제 상태 통계는 현재 DB 기준
		List<FestivalStatusStatDTO> festivalStatusStats =
				adminDashboardDAO.selectFestivalStatusStats();
		applyFestivalStatusPercent(festivalStatusStats);
		dto.setFestivalStatusStats(festivalStatusStats);

		// 지역별 축제 통계는 현재 DB 기준
		List<RegionStatDTO> regionStats =
				adminDashboardDAO.selectRegionStats();
		applyRegionPercent(regionStats);
		dto.setRegionStats(regionStats);

		// 나머지 운영 데이터도 현재 DB 기준
		dto.setPopularFestivals(adminDashboardDAO.selectPopularFestivals());
		dto.setRecentReports(adminDashboardDAO.selectRecentReports());
		dto.setRecentIssues(adminDashboardDAO.selectRecentIssues());

		return dto;
	}

	// 기준일이 없거나 잘못된 형식이면 오늘 날짜 사용, 미래 날짜도 오늘 날짜로 처리
	private String normalizeBaseDate(String baseDate) {
		LocalDate today = LocalDate.now();

		if (baseDate == null || baseDate.isBlank()) {
			return today.toString();
		}

		try {
			LocalDate parsedDate = LocalDate.parse(baseDate);

			if (parsedDate.isAfter(today)) {
				return today.toString();
			}

			return parsedDate.toString();
		} catch (DateTimeParseException e) {
			return today.toString();
		}
	}

	// 축제 상태별 비율 계산
	private void applyFestivalStatusPercent(List<FestivalStatusStatDTO> list) {
		if (list == null || list.isEmpty()) return;

		int total = list.stream()
				.mapToInt(FestivalStatusStatDTO::getCount)
				.sum();

		if (total == 0) return;

		for (FestivalStatusStatDTO item : list) {
			int percent = (int) Math.round((item.getCount() * 100.0) / total);
			item.setPercent(percent);
		}
	}

	// 지역별 통계 비율 계산
	private void applyRegionPercent(List<RegionStatDTO> list) {
		if (list == null || list.isEmpty()) return;

		int max = list.stream()
				.mapToInt(RegionStatDTO::getCount)
				.max()
				.orElse(0);

		if (max == 0) return;

		for (RegionStatDTO item : list) {
			int percent = (int) Math.round((item.getCount() * 100.0) / max);
			item.setPercent(percent);
		}
	}

}
