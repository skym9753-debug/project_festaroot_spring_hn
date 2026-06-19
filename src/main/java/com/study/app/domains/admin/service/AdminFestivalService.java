package com.study.app.domains.admin.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.festival.FestivalDAO;
import com.study.app.domains.festival.dto.FestivalDTO;

@Service
public class AdminFestivalService {
	
	@Autowired
	private FestivalDAO festivalDAO;

	@Autowired
	private com.study.app.domains.festival.FestivalService festivalService;

	@Autowired
	private com.study.app.domains.festival.ThemeMappingService themeMappingService;

	@Autowired
	private com.study.app.domains.festival.VectorIndexingService vectorIndexingService;

	private final Map<String, Object> syncStatus = new ConcurrentHashMap<>();

	public AdminFestivalService() {
		syncStatus.put("status", "IDLE");
		syncStatus.put("message", "대기 중");
		syncStatus.put("lastSyncTime", "");
	}

	public Map<String, Object> getSyncStatus() {
		return syncStatus;
	}

	public synchronized boolean startSyncPipeline() {
		if ("RUNNING".equals(syncStatus.get("status"))) {
			return false;
		}

		syncStatus.put("status", "RUNNING");
		syncStatus.put("message", "데이터 동기화 파이프라인 시작 중...");

		CompletableFuture.runAsync(() -> {
			try {
				syncStatus.put("message", "1단계: 축제 API 데이터 동기화 진행 중...");
				festivalService.saveFestivalInfoFromApi();

				syncStatus.put("message", "2단계: AI 테마 매핑 진행 중...");
				themeMappingService.mapThemesForAllFestivals();

				syncStatus.put("message", "3단계: Qdrant 벡터화 및 인덱싱 진행 중...");
				vectorIndexingService.indexAllFestivals();

				syncStatus.put("status", "COMPLETED");
				syncStatus.put("message", "전체 동기화 파이프라인이 성공적으로 완료되었습니다.");
				syncStatus.put("lastSyncTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			} catch (Exception e) {
				syncStatus.put("status", "FAILED");
				syncStatus.put("message", "동기화 중 오류 발생: " + e.getMessage());
				e.printStackTrace();
			}
		});

		return true;
	}
	
	public List<FestivalDTO> getAllFestivals(){
		return festivalDAO.getAllFestivalAdmin();
	}
	
	@Transactional
	public void updateVisibility(Long contentId, String isVisible) {
		festivalDAO.updateVisibility(contentId, isVisible);
	}
	
}
