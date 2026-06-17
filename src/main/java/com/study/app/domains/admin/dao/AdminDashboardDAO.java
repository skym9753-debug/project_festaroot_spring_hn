package com.study.app.domains.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.admin.dto.AdminSummaryDTO;
import com.study.app.domains.admin.dto.FestivalStatusStatDTO;
import com.study.app.domains.admin.dto.PopularFestivalDTO;
import com.study.app.domains.admin.dto.RecentIssueDTO;
import com.study.app.domains.admin.dto.RecentReportDTO;
import com.study.app.domains.admin.dto.RegionStatDTO;
import com.study.app.domains.admin.dto.WeeklyStatDTO;

@Repository
public class AdminDashboardDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
    // mapper XML의 namespace
    private static final String NAMESPACE = "AdminDashboard";

    // 최근 7일 통계 기준일 파라미터 생성
    private Map<String, Object> getBaseDateParam(String baseDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("baseDate", baseDate);
        return param;
    }

    // 상단 핵심 통계
    public AdminSummaryDTO selectSummary() {
        return mybatis.selectOne(NAMESPACE + ".selectSummary");
    }

    // 최근 7일 운영 통계
    public List<WeeklyStatDTO> selectWeeklyStats(String baseDate) {
        return mybatis.selectList(
                NAMESPACE + ".selectWeeklyStats",
                getBaseDateParam(baseDate)
        );
    }

    // 축제 상태 통계
    public List<FestivalStatusStatDTO> selectFestivalStatusStats() {
        return mybatis.selectList(NAMESPACE + ".selectFestivalStatusStats");
    }

    // 지역별 축제 데이터 TOP 5
    public List<RegionStatDTO> selectRegionStats() {
        return mybatis.selectList(NAMESPACE + ".selectRegionStats");
    }

    // 인기 축제 TOP 5
    public List<PopularFestivalDTO> selectPopularFestivals() {
        return mybatis.selectList(NAMESPACE + ".selectPopularFestivals");
    }

    // 최근 신고 접수
    public List<RecentReportDTO> selectRecentReports() {
        return mybatis.selectList(NAMESPACE + ".selectRecentReports");
    }

    // 최근 운영 이슈
    public List<RecentIssueDTO> selectRecentIssues() {
        return mybatis.selectList(NAMESPACE + ".selectRecentIssues");
    }
}
	
	
