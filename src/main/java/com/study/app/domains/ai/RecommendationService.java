package com.study.app.domains.ai;

import java.time.LocalDate;
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
     * 유저 맞춤형 축제 추천 로직 (찜한 축제 + 유저 한마디 + 관심 지역/테마 반영)
     */
    public List<Map<String, Object>> getPersonalizedRecommendations(String memberId, String userInput) {
        try {
            // 0. 유저 한마디 활동 로그 저장 (나중에 취향 분석에 활용)
            if (userInput != null && !userInput.trim().isEmpty()) {
                UserActivityLogDTO logDTO = new UserActivityLogDTO();
                logDTO.setMember_id(memberId);
                logDTO.setAction_type("SEARCH");
                logDTO.setKeyword(userInput);
                activityLogService.saveLog(logDTO);
            }

            // 1. 유저 정보 조회 (기본 프로필 + 관심 지역 + 관심 테마 + 찜한 축제)
            MemberDTO member = memberDAO.selectMemberById(memberId);
            List<InterestRegionDTO> interestRegions = memberDAO.selectInterestRegions(memberId);
            List<InterestThemeDTO> interestThemes = memberDAO.selectInterestThemes(memberId);
            List<UserActivityLogDTO> recentLogs = activityLogService.getRecentLogs(memberId);
            List<Map<String, Object>> likedFestivals = festivalDAO.getMyFestivalLikedDetails(memberId);
            
            // 2. 추천 컨텍스트 구성 (찜한 목록 및 유저 입력 반영)
            String userContext = buildUserContext(member, interestRegions, interestThemes, recentLogs, likedFestivals, userInput);
            log.info("User Context: {}", userContext);

            // 3. 후보군 수집 (유저 입력 기반 Qdrant 검색 우선 + 관심사 및 거주지 기반)
            List<Map<String, Object>> candidates = collectCandidates(member, interestRegions, interestThemes, recentLogs, userInput);
            
            // 후보군이 너무 적으면 인기 축제로 보충
            if (candidates.size() < 5) {
                candidates.addAll(festivalDAO.getPopularFestivals());
            }

            // 4. Gemini에게 최종 추천 3~5개와 이유 요청
            String prompt = buildRecommendationPrompt(userContext, candidates, userInput);
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

    private String buildUserContext(MemberDTO member, List<InterestRegionDTO> regions, List<InterestThemeDTO> themes, 
                                   List<UserActivityLogDTO> logs, List<Map<String, Object>> likedFestivals, String userInput) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("유저 정보: %s, %s. ", member.getGender(), member.getBirthdate()));
        if (member.getAddr_sido() != null && !member.getAddr_sido().trim().isEmpty()) {
            sb.append(String.format("거주 지역: %s %s. ", member.getAddr_sido(), 
                (member.getAddr_sigungu() != null ? member.getAddr_sigungu() : "")));
        }
        
        if (userInput != null && !userInput.trim().isEmpty()) {
            sb.append("현재 유저의 요청(한마디): \"").append(userInput).append("\". ");
        }

        // 찜한 목록 (장기 취향)
        if (likedFestivals != null && !likedFestivals.isEmpty()) {
            String likedTitles = likedFestivals.stream()
                .map(f -> String.valueOf(f.get("TITLE")))
                .limit(5)
                .collect(Collectors.joining(", "));
            sb.append("유저가 평소 찜한 축제: ").append(likedTitles).append(". ");
        }

        // AI 추천 피드백 반영 (최근 취향 교정)
        if (logs != null && !logs.isEmpty()) {
            String aiLikes = logs.stream()
                .filter(l -> "AI_LIKE".equals(l.getAction_type()))
                .map(l -> l.getTitle())
                .limit(3)
                .collect(Collectors.joining(", "));
            if (!aiLikes.isEmpty()) sb.append("AI 추천 중 좋아한 축제: ").append(aiLikes).append(". ");

            String aiDislikes = logs.stream()
                .filter(l -> "AI_DISLIKE".equals(l.getAction_type()))
                .map(l -> l.getTitle() + (l.getKeyword() != null ? "(" + l.getKeyword() + ")" : ""))
                .limit(3)
                .collect(Collectors.joining(", "));
            if (!aiDislikes.isEmpty()) sb.append("AI 추천 중 거절한 축제(사유): ").append(aiDislikes).append(". ");
        }

        if (!regions.isEmpty()) {
            String regionStr = regions.stream().map(r -> r.getRegion_code()).collect(Collectors.joining(", "));
            sb.append("관심 지역 코드: ").append(regionStr).append(". ");
        }
        
        if (!themes.isEmpty()) {
            String themeStr = themes.stream().map(t -> t.getTheme_code()).collect(Collectors.joining(", "));
            sb.append("관심 테마 코드: ").append(themeStr).append(". ");
        }
        
        return sb.toString();
    }

    private List<Map<String, Object>> collectCandidates(MemberDTO member, List<InterestRegionDTO> regions, List<InterestThemeDTO> themes, 
                                                       List<UserActivityLogDTO> logs, String userInput) {
        Set<Long> uniqueIds = new HashSet<>();
        Set<Long> forbiddenIds = new HashSet<>();
        List<Map<String, Object>> candidates = new ArrayList<>();

        // 0. 싫어요(AI_DISLIKE) 누른 축제는 후보군에서 원천 배제
        if (logs != null) {
            logs.stream()
                .filter(l -> "AI_DISLIKE".equals(l.getAction_type()) && l.getContent_id() != null)
                .forEach(l -> forbiddenIds.add(l.getContent_id()));
        }

        // 1. 유저의 '한마디(userInput)' 기반 Qdrant 검색 - 최우선 순위
        if (userInput != null && !userInput.trim().isEmpty()) {
            try {
                List<Double> vector = geminiService.getEmbedding(userInput);
                List<Long> similarIds = vectorIndexingService.searchSimilarFestivals(vector, 15);
                for (Long id : similarIds) {
                    if (!forbiddenIds.contains(id) && uniqueIds.add(id)) {
                        Map<String, Object> fest = festivalDAO.getFestivalDetail(id);
                        if (fest != null) candidates.add(fest);
                    }
                }
            } catch (Exception e) {
                log.error("유저 입력 기반 Qdrant 검색 중 오류: {}", e.getMessage());
            }
        }

        // 2. Qdrant 벡터 기반 유사 추천 (최근 활동 기반 - AI_LIKE 포함)
        if (candidates.size() < 20 && logs != null && !logs.isEmpty()) {
            try {
                // 최근 좋아한 축제(AI_LIKE)나 검색 로그 기반
                UserActivityLogDTO pivotLog = logs.stream()
                    .filter(l -> "AI_LIKE".equals(l.getAction_type()) || "SEARCH".equals(l.getAction_type()))
                    .findFirst().orElse(logs.get(0));

                String queryText = "";
                if ("SEARCH".equals(pivotLog.getAction_type())) {
                    queryText = pivotLog.getKeyword();
                } else if (pivotLog.getContent_id() != null) {
                    Map<String, Object> fest = festivalDAO.getFestivalDetail(pivotLog.getContent_id());
                    if (fest != null) {
                        queryText = String.valueOf(fest.get("TITLE")) + " " + convertToString(fest.get("OVERVIEW"));
                    }
                }

                if (!queryText.isEmpty()) {
                    List<Double> vector = geminiService.getEmbedding(queryText);
                    List<Long> similarIds = vectorIndexingService.searchSimilarFestivals(vector, 10);
                    for (Long id : similarIds) {
                        if (!forbiddenIds.contains(id) && uniqueIds.add(id)) {
                            Map<String, Object> fest = festivalDAO.getFestivalDetail(id);
                            if (fest != null) candidates.add(fest);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Qdrant 추천 후보 수집 중 오류: {}", e.getMessage());
            }
        }

        // 2.5. 거주 지역 기반 후보 (로그인 유저 거주지 매핑)
        if (candidates.size() < 30 && member != null && member.getReside_area_code() != null && !member.getReside_area_code().trim().isEmpty()) {
            try {
                List<Map<String, Object>> resideFests = festivalDAO.getFestivalsByRegion(member.getReside_area_code());
                for (Map<String, Object> f : resideFests) {
                    Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                    if (!forbiddenIds.contains(id) && uniqueIds.add(id)) candidates.add(f);
                }
            } catch (Exception e) {
                log.error("거주 지역 기반 후보 수집 중 오류: {}", e.getMessage());
            }
        }

        // 3. 관심 지역 기반 후보
        if (candidates.size() < 30) {
            for (InterestRegionDTO region : regions) {
                List<Map<String, Object>> regionFests = festivalDAO.getFestivalsByRegion(region.getRegion_code());
                for (Map<String, Object> f : regionFests) {
                    Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                    if (!forbiddenIds.contains(id) && uniqueIds.add(id)) candidates.add(f);
                }
                if (candidates.size() >= 30) break;
            }
        }

        // 4. 관심 테마 기반 후보
        if (candidates.size() < 40 && !themes.isEmpty()) {
            List<String> themeCodes = themes.stream().map(t -> t.getTheme_code()).collect(Collectors.toList());
            List<Map<String, Object>> themeFests = festivalDAO.getFestivalsByThemes(themeCodes);
            for (Map<String, Object> f : themeFests) {
                Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                if (!forbiddenIds.contains(id) && uniqueIds.add(id)) candidates.add(f);
            }
        }

        // 4.5. 유저 입력(userInput)에 특정 지역 정보가 포함된 경우 해당 지역 축제로만 하드 필터링
        String targetRegionCode = extractRegionCodeFromInput(userInput);
        if (targetRegionCode != null) {
            log.info("유저 입력에서 지역명 감지. 대상 지역코드: {}", targetRegionCode);
            candidates = candidates.stream()
                .filter(f -> targetRegionCode.equals(String.valueOf(f.get("REGION_CODE"))))
                .collect(Collectors.toList());
            
            // 필터링 결과 후보가 부족한 경우 해당 지역의 축제를 DB에서 가져와 보충
            if (candidates.size() < 10) {
                List<Map<String, Object>> regionFests = festivalDAO.getFestivalsByRegion(targetRegionCode);
                for (Map<String, Object> f : regionFests) {
                    Long id = Long.parseLong(String.valueOf(f.get("CONTENT_ID")));
                    if (!forbiddenIds.contains(id) && uniqueIds.add(id)) {
                        candidates.add(f);
                    }
                }
            }
        }
        
        return candidates.stream().limit(40).collect(Collectors.toList());
    }

    private String buildRecommendationPrompt(String userContext, List<Map<String, Object>> candidates, String userInput) {
        String today = LocalDate.now().toString();
        
        String candidatesJson = candidates.stream()
            .map(c -> String.format("{\"id\": %s, \"title\": \"%s\", \"start\": \"%s\", \"end\": \"%s\", \"overview\": \"%s\"}", 
                c.get("CONTENT_ID"), c.get("TITLE"), c.get("EVENT_START_DATE"), c.get("EVENT_END_DATE"), c.get("OVERVIEW")))
            .collect(Collectors.joining(", ", "[", "]"));

        String userInstruction = (userInput != null && !userInput.trim().isEmpty()) 
            ? String.format("유저가 요구한 '%s'라는 요청사항을 최우선으로 반영해줘. 시간이나 장소에 대한 언급이 있다면 [후보 리스트]의 날짜와 비교하여 가장 적절한 것을 골라줘.", userInput)
            : "유저의 평소 관심사와 찜한 목록을 바탕으로 현재 시점에 즐기기 좋은 축제를 추천해줘.";

        return String.format(
            "너는 대한민국 축제 추천 전문가야. 오늘 날짜는 [%s]이야.\n\n" +
            "아래 [유저 컨텍스트]를 분석하여 [후보 리스트] 중 유저에게 가장 만족도가 높을 축제 3~5개를 선정해줘.\n\n" +
            "[유저 컨텍스트]: %s\n" +
            "[후보 리스트]: %s\n\n" +
            "[지침]:\n" +
            "1. %s\n" +
            "2. 유저의 거주 지역 정보가 있다면, 가급적 거주지 인근 또는 이동하기 편리한 지역의 축제를 우선적으로 추천하고, 추천 이유(recommendation_reason)에도 유저 거주지를 고려했음을 자연스럽게 설명해줘.\n" +
            "3. 이미 종료된 축제(end 날짜가 오늘보다 이전)는 추천에서 가급적 제외해줘.\n" +
            "4. 유저가 '싫어요'라고 피드백한 축제나 사유가 있다면 해당 스타일은 반드시 제외해줘.\n" +
            "5. 유저가 '좋아요'한 축제가 있다면 그와 유사한 테마나 분위기를 적극 반영해줘.\n" +
            "6. 반드시 JSON 배열 형식으로만 응답해줘.\n" +
            "7. recommendation_reason은 유저의 요청사항, 과거 피드백(좋아요/싫어요), 거주지 정보, 시기적 적절성을 연계하여 '해요'체로 친절하게 작성해줘.\n" +
            "형식: [{\"content_id\": 123, \"recommendation_reason\": \"...\"}]",
            today, userContext, candidatesJson, userInstruction
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

    private String extractRegionCodeFromInput(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return null;
        }
        
        String cleanInput = userInput.replaceAll("\\s+", "");
        
        if (cleanInput.contains("서울")) return "11";
        if (cleanInput.contains("부산")) return "26";
        if (cleanInput.contains("대구")) return "27";
        if (cleanInput.contains("인천")) return "28";
        if (cleanInput.contains("광주")) return "29";
        if (cleanInput.contains("대전")) return "30";
        if (cleanInput.contains("울산")) return "31";
        if (cleanInput.contains("세종")) return "36";
        if (cleanInput.contains("경기")) return "41";
        if (cleanInput.contains("강원")) return "42";
        if (cleanInput.contains("충북") || cleanInput.contains("충청북도")) return "43";
        if (cleanInput.contains("충남") || cleanInput.contains("충청남도")) return "44";
        if (cleanInput.contains("전북") || cleanInput.contains("전라북도") || cleanInput.contains("전북특별자치도")) return "45";
        if (cleanInput.contains("전남") || cleanInput.contains("전라남도")) return "46";
        if (cleanInput.contains("경북") || cleanInput.contains("경상북도")) return "47";
        if (cleanInput.contains("경남") || cleanInput.contains("경상남도")) return "48";
        if (cleanInput.contains("제주")) return "50";
        
        return null;
    }
}
