package com.study.app.domains.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.app.domains.activity.UserActivityLogService;
import com.study.app.domains.activity.dto.UserActivityLogDTO;
import com.study.app.domains.festival.FestivalDAO;
import com.study.app.domains.festival.VectorIndexingService;
import com.study.app.domains.member.MemberDAO;
import com.study.app.domains.member.dto.InterestRegionDTO;
import com.study.app.domains.member.dto.InterestThemeDTO;
import com.study.app.domains.member.dto.MemberDTO;
import com.study.app.utils.GeminiService;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired private MemberDAO memberDAO;
    @Autowired private UserActivityLogService activityLogService;
    @Autowired private FestivalDAO festivalDAO;
    @Autowired private GeminiService geminiService;
    @Autowired private VectorIndexingService vectorIndexingService;
    @Autowired private ObjectMapper objectMapper;

    /**
     * 유저 맞춤형 축제 추천 로직 (관심 지역/테마 반영 + Qdrant 벡터 검색 통합)
     */
    public List<Map<String, Object>> getPersonalizedRecommendations(String memberId) {
        try {
            // 1. 유저 정보 조회 (기본 프로필 + 관심 지역 + 관심 테마)
            MemberDTO member = memberDAO.selectMemberById(memberId);
            List<InterestRegionDTO> interestRegions = memberDAO.selectInterestRegions(memberId);
            List<InterestThemeDTO> interestThemes = memberDAO.selectInterestThemes(memberId);
            List<UserActivityLogDTO> recentLogs = activityLogService.getRecentLogs(memberId);
            
            // 2. 추천 컨텍스트 구성
            String userContext = buildUserContext(member, interestRegions, interestThemes, recentLogs);
            log.info("User Context: {}", userContext);

            // 3. 후보군 수집 (Qdrant 벡터 검색 + 관심 지역 + 관심 테마 기반)
            List<Map<String, Object>> candidates = collectCandidates(interestRegions, interestThemes, recentLogs);
            
            // 후보군이 너무 적으면 인기 축제로 보충
            if (candidates.size() < 5) {
                candidates.addAll(festivalDAO.getPopularFestivals());
            }

            // 4. Gemini에게 최종 추천 3~5개와 이유 요청
            String prompt = buildRecommendationPrompt(userContext, candidates);
            String aiResponse = geminiService.getCompletion(prompt);
            
            // AI 응답 파싱
            String jsonContent = aiResponse.replaceAll("```json|```", "").trim();
            List<Map<String, Object>> recommendedList = objectMapper.readValue(jsonContent, new TypeReference<>() {});

            // 5. 상세 정보 결합
            return enrichRecommendationData(recommendedList);

        } catch (Exception e) {
            log.error("추천 생성 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String buildUserContext(MemberDTO member, List<InterestRegionDTO> regions, List<InterestThemeDTO> themes, List<UserActivityLogDTO> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("유저 정보: %s, %s. ", member.getGender(), member.getBirthdate()));
        
        if (!regions.isEmpty()) {
            String regionStr = regions.stream().map(r -> r.getRegion_code()).collect(Collectors.joining(", "));
            sb.append("관심 지역 코드: ").append(regionStr).append(". ");
        }
        
        if (!themes.isEmpty()) {
            String themeStr = themes.stream().map(t -> t.getTheme_code()).collect(Collectors.joining(", "));
            sb.append("관심 테마 코드: ").append(themeStr).append(". ");
        }
        
        if (logs != null && !logs.isEmpty()) {
            String recentActivities = logs.stream()
                .map(l -> "ACTION:" + l.getAction_type() + (l.getKeyword() != null ? ", KEYWORD:" + l.getKeyword() : ", ID:" + l.getContent_id()))
                .limit(5)
                .collect(Collectors.joining(" | "));
            sb.append("최근 활동 로그: ").append(recentActivities);
        } else {
            sb.append("최근 활동 로그 없음. ");
        }
        
        return sb.toString();
    }

    private List<Map<String, Object>> collectCandidates(List<InterestRegionDTO> regions, List<InterestThemeDTO> themes, List<UserActivityLogDTO> logs) {
        Set<Long> uniqueIds = new HashSet<>();
        List<Map<String, Object>> candidates = new ArrayList<>();

        // 1. Qdrant 벡터 기반 유사 추천 (최근 활동 기반) - 가장 강력한 추천
        if (logs != null && !logs.isEmpty()) {
            try {
                UserActivityLogDTO latestLog = logs.get(0);
                String queryText = "";
                
                if ("SEARCH".equals(latestLog.getAction_type())) {
                    queryText = latestLog.getKeyword();
                } else if (latestLog.getContent_id() != null) {
                    Map<String, Object> fest = festivalDAO.getFestivalDetail(latestLog.getContent_id());
                    if (fest != null) {
                        queryText = String.valueOf(fest.get("TITLE")) + " " + convertToString(fest.get("OVERVIEW"));
                    }
                }

                if (!queryText.isEmpty()) {
                    List<Double> vector = geminiService.getEmbedding(queryText);
                    List<Long> similarIds = vectorIndexingService.searchSimilarFestivals(vector, 10);
                    for (Long id : similarIds) {
                        if (uniqueIds.add(id)) {
                            Map<String, Object> fest = festivalDAO.getFestivalDetail(id);
                            if (fest != null) candidates.add(fest);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Qdrant 추천 후보 수집 중 오류: {}", e.getMessage());
            }
        }

        // 2. 관심 지역 기반 후보
        for (InterestRegionDTO region : regions) {
            List<Map<String, Object>> regionFests = festivalDAO.getFestivalsByRegion(region.getRegion_code());
            for (Map<String, Object> f : regionFests) {
                Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                if (uniqueIds.add(id)) candidates.add(f);
            }
            if (candidates.size() >= 20) break;
        }

        // 3. 관심 테마 기반 후보
        if (!themes.isEmpty()) {
            List<String> themeCodes = themes.stream().map(t -> t.getTheme_code()).collect(Collectors.toList());
            List<Map<String, Object>> themeFests = festivalDAO.getFestivalsByThemes(themeCodes);
            for (Map<String, Object> f : themeFests) {
                Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                if (uniqueIds.add(id)) candidates.add(f);
            }
        }
        
        return candidates.stream().limit(30).collect(Collectors.toList());
    }

    private String buildRecommendationPrompt(String userContext, List<Map<String, Object>> candidates) {
        String candidatesJson = candidates.stream()
            .map(c -> String.format("{\"id\": %s, \"title\": \"%s\", \"overview\": \"%s\"}", 
                c.get("CONTENT_ID"), c.get("TITLE"), c.get("OVERVIEW")))
            .collect(Collectors.joining(", ", "[", "]"));

        return String.format(
            "너는 대한민국 축제 추천 전문가야. 아래 [유저 컨텍스트]를 분석하여 [후보 리스트] 중 가장 만족도가 높을 것 같은 축제 3~5개를 선정해줘.\n\n" +
            "[유저 컨텍스트]: %s\n" +
            "[후보 리스트]: %s\n\n" +
            "반드시 JSON 배열 형식으로만 응답해줘. recommendation_reason은 유저의 관심 지역, 테마, 혹은 최근 활동 로그와의 연관성을 구체적으로 언급하며 '해요'체로 친절하게 작성해줘.\n" +
            "형식: [{\"content_id\": 123, \"recommendation_reason\": \"...\"}]",
            userContext, candidatesJson
        );
    }

    private List<Map<String, Object>> enrichRecommendationData(List<Map<String, Object>> recommendedList) {
        List<Map<String, Object>> finalResult = new ArrayList<>();
        for (Map<String, Object> rec : recommendedList) {
            Object idObj = rec.get("content_id");
            if (idObj == null) continue;
            Long contentId = Long.parseLong(String.valueOf(idObj));
            Map<String, Object> festivalDetail = festivalDAO.getFestivalDetail(contentId);
            if (festivalDetail != null) {
                Map<String, Object> cleanData = new HashMap<>();
                for (Map.Entry<String, Object> entry : festivalDetail.entrySet()) {
                    cleanData.put(entry.getKey(), convertToString(entry.getValue()));
                }
                cleanData.put("recommendation_reason", rec.get("recommendation_reason"));
                finalResult.add(cleanData);
            }
        }
        return finalResult;
    }

    private String convertToString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof java.sql.Clob) {
            try {
                java.sql.Clob clob = (java.sql.Clob) obj;
                StringBuilder sb = new StringBuilder();
                try (java.io.Reader reader = clob.getCharacterStream();
                     java.io.BufferedReader br = new java.io.BufferedReader(reader)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString().trim();
            } catch (Exception e) {
                log.error("CLOB 변환 오류: {}", e.getMessage());
                return "";
            }
        }
        return String.valueOf(obj);
    }
}
