package com.study.app.domains.ai;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.app.domains.ai.dto.AIPlannerDTO;
import com.study.app.domains.ai.dto.AIPlannerStepDTO;
import com.study.app.domains.festival.FestivalService;
import com.study.app.domains.festival.dto.FestivalDTO;
import com.study.app.domains.festival.dto.NearbyPlaceDTO;
import com.study.app.utils.GeminiService;

@Service
public class PlannerService {

	private static final String PLANNER_TYPE = "AI_PLANNER";
	private static final String DEFAULT_COURSE_STYLE = "RELAXED";

	private static final String ROUTE_NOTICE =
			"표시되는 순서는 실제 길찾기 경로나 이동시간 계산이 아닌, 축제장 주변 장소와 날씨를 반영해 AI가 구성한 추천 방문 순서입니다.";

	private static final String WEATHER_FAIL_MESSAGE =
			"방문일이 예보 범위를 벗어나거나 날씨 정보를 조회하지 못해 날씨는 반영하지 않았습니다.";

	private static final String PLANNER_INSERT_FAIL_MESSAGE =
			"planner 저장에 실패했습니다.";

	private static final String GEMINI_EMPTY_MESSAGE =
			"Gemini가 플래너 결과를 생성하지 못했습니다.";

	private static final String GEMINI_PARSE_FAIL_MESSAGE =
			"Gemini 응답 파싱에 실패했습니다. JSON 배열 형식을 확인해 주세요.";

	private static final String STEP_INSERT_FAIL_MESSAGE =
			"Gemini 생성 플래너 step 저장에 실패했습니다.";

	private final PlannerDAO plannerDAO;
	private final FestivalService festivalService;
	private final WeatherService weatherService;
	private final GeminiService geminiService;
	private final ObjectMapper objectMapper;

	public PlannerService(
			PlannerDAO plannerDAO,
			FestivalService festivalService,
			WeatherService weatherService,
			GeminiService geminiService,
			ObjectMapper objectMapper
			) {
		this.plannerDAO = plannerDAO;
		this.festivalService = festivalService;
		this.weatherService = weatherService;
		this.geminiService = geminiService;
		this.objectMapper = objectMapper;
	}

	/**
	 * AI 축제 플래너 생성
	 *
	 * 최종 흐름:
	 * 1. content_id로 festival_info DB 조회
	 * 2. 축제 기간/좌표 검증
	 * 3. WeatherService로 방문일 날씨 조회
	 * 4. TourAPI로 축제장 주변 장소 후보 조회
	 * 5. Gemini API에 축제/날씨/주변장소/사용자조건 전달
	 * 6. Gemini 응답 JSON 파싱
	 * 7. ai_planner 저장
	 * 8. ai_planner_step 저장
	 */
	
	
	
	
	/**
	 * AI 플래너 미리보기 생성
	 *
	 * DB에 저장하지 않고 화면에 보여줄 코스만 생성한다.
	 */
	public HashMap<String, Object> previewPlanner(AIPlannerDTO plannerDTO, String memberId) {

	    HashMap<String, Object> result = new HashMap<>();

	    HashMap<String, Object> validation = validatePlannerInput(plannerDTO);
	    if (validation != null) {
	        return validation;
	    }

	    System.out.println("AI 플래너 미리보기 요청 content_id = " + plannerDTO.getContent_id());

	    FestivalDTO festival = plannerDAO.selectFestivalByContentId(plannerDTO.getContent_id());

	    System.out.println("festival_info 조회 결과 = " + festival);

	    if (festival == null) {
	        result.put("success", false);
	        result.put("message", "축제 정보를 확인할 수 없습니다.");
	        return result;
	    }

	    if (festival.getMap_x() == null || festival.getMap_y() == null) {
	        result.put("success", false);
	        result.put("message", "축제 좌표 정보가 없어 AI 플래너를 생성할 수 없습니다.");
	        return result;
	    }

	    HashMap<String, Object> periodValidation = validateFestivalPeriod(
	            plannerDTO.getVisit_date(),
	            festival.getEvent_start_date(),
	            festival.getEvent_end_date()
	    );

	    if (periodValidation != null) {
	        return periodValidation;
	    }

	    // 1. 날씨 API 조회
	    String weatherSummary = getWeatherSummarySafe(
	            plannerDTO.getVisit_date(),
	            festival.getMap_x(),
	            festival.getMap_y(),
	            festival.getAddr1()
	            
	    );

	    // 2. TourAPI 주변 장소 조회
	    Double lat = festival.getMap_y();
	    Double lng = festival.getMap_x();
	    Integer radius = 3000;

	    List<NearbyPlaceDTO> nearbyFoods = getNearbyPlacesSafe(lat, lng, radius, "39");
	    List<NearbyPlaceDTO> nearbyTours = getNearbyPlacesSafe(lat, lng, radius, "12");
	    List<NearbyPlaceDTO> nearbyCultures = getNearbyPlacesSafe(lat, lng, radius, "14");

	    // 3. 기본 정보 세팅
	    plannerDTO.setMember_id(memberId);
	    plannerDTO.setTitle(festival.getTitle() + " AI 플래너");
	    plannerDTO.setFestival_title(festival.getTitle());
	    plannerDTO.setFirst_image(festival.getFirst_image());
	    plannerDTO.setAddr1(festival.getAddr1());
	    plannerDTO.setMap_x(String.valueOf(festival.getMap_x()));
	    plannerDTO.setMap_y(String.valueOf(festival.getMap_y()));
	    plannerDTO.setWeather_summary(weatherSummary);
	    plannerDTO.setPlanner_type(PLANNER_TYPE);

	    if (isBlank(plannerDTO.getCourse_style())) {
	        plannerDTO.setCourse_style(DEFAULT_COURSE_STYLE);
	    }

	    plannerDTO.setRoute_notice(ROUTE_NOTICE);

	    // 4. Gemini 프롬프트 생성
	    String prompt = buildGeminiPlannerPrompt(
	            plannerDTO,
	            festival,
	            weatherSummary,
	            nearbyFoods,
	            nearbyTours,
	            nearbyCultures
	    );

	    // 5. Gemini API 호출
	    String geminiResponse = geminiService.getCompletion(prompt);

	    System.out.println("Gemini planner raw response = " + geminiResponse);

	    if (isBlank(geminiResponse)) {
	        result.put("success", false);
	        result.put("message", GEMINI_EMPTY_MESSAGE);
	        return result;
	    }

	    // 6. Gemini 응답 JSON 배열 추출
	    String jsonArray = extractJsonArray(geminiResponse);

	    if (jsonArray == null) {
	        result.put("success", false);
	        result.put("message", GEMINI_PARSE_FAIL_MESSAGE);
	        result.put("gemini_raw", geminiResponse);
	        return result;
	    }

	    List<AIPlannerStepDTO> steps;

	    try {
	        steps = objectMapper.readValue(
	                jsonArray,
	                new TypeReference<List<AIPlannerStepDTO>>() {}
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	        result.put("success", false);
	        result.put("message", GEMINI_PARSE_FAIL_MESSAGE);
	        result.put("gemini_raw", geminiResponse);
	        return result;
	    }

	    if (steps == null || steps.isEmpty()) {
	        result.put("success", false);
	        result.put("message", GEMINI_PARSE_FAIL_MESSAGE);
	        return result;
	    }

	    // 7. Gemini 결과 보정
	    normalizeGeminiSteps(steps, festival);

	    /*
	     * 중요:
	     * 여기서는 DB 저장하지 않는다.
	     * 즉 insertPlanner, insertPlannerStep 호출 없음.
	     */

	    result.put("success", true);
	    result.put("message", "AI 플래너 미리보기가 생성되었습니다.");
	    result.put("planner_type", plannerDTO.getPlanner_type());
	    result.put("title", plannerDTO.getTitle());
	    result.put("festival_title", plannerDTO.getFestival_title());
	    result.put("first_image", plannerDTO.getFirst_image());
	    result.put("addr1", plannerDTO.getAddr1());
	    result.put("map_x", plannerDTO.getMap_x());
	    result.put("map_y", plannerDTO.getMap_y());
	    result.put("course_style", plannerDTO.getCourse_style());
	    result.put("weather_summary", weatherSummary);
	    result.put("route_notice", plannerDTO.getRoute_notice());
	    result.put("steps", steps);

	    return result;
	}
	
	/**
	 * AI 플래너 저장
	 *
	 * 프론트에서 미리보기로 확인한 코스를 마이페이지에 저장한다.
	 */
	@Transactional
	public HashMap<String, Object> savePlanner(AIPlannerDTO plannerDTO, String memberId) {

	    HashMap<String, Object> result = new HashMap<>();

	    if (plannerDTO == null) {
	        result.put("success", false);
	        result.put("message", "저장할 플래너 정보가 없습니다.");
	        return result;
	    }

	    if (plannerDTO.getContent_id() == null) {
	        result.put("success", false);
	        result.put("message", "content_id는 필수입니다.");
	        return result;
	    }

	    if (plannerDTO.getVisit_date() == null) {
	        result.put("success", false);
	        result.put("message", "방문 날짜는 필수입니다.");
	        return result;
	    }

	    if (plannerDTO.getSteps() == null || plannerDTO.getSteps().isEmpty()) {
	        result.put("success", false);
	        result.put("message", "저장할 코스 step 정보가 없습니다.");
	        return result;
	    }

	    plannerDTO.setMember_id(memberId);

	    if (isBlank(plannerDTO.getTitle())) {
	        plannerDTO.setTitle("AI 축제 플래너");
	    }

	    if (isBlank(plannerDTO.getPlanner_type())) {
	        plannerDTO.setPlanner_type(PLANNER_TYPE);
	    }

	    if (isBlank(plannerDTO.getCourse_style())) {
	        plannerDTO.setCourse_style(DEFAULT_COURSE_STYLE);
	    }

	    if (isBlank(plannerDTO.getRoute_notice())) {
	        plannerDTO.setRoute_notice(ROUTE_NOTICE);
	    }

	    // 1. ai_planner 저장
	    int plannerInsertResult = plannerDAO.insertPlanner(plannerDTO);

	    if (plannerInsertResult == 0 || plannerDTO.getPlanner_id() == null) {
	        result.put("success", false);
	        result.put("message", PLANNER_INSERT_FAIL_MESSAGE);
	        return result;
	    }

	    // 2. ai_planner_step 저장
	    int insertedStepCount = 0;

	    for (AIPlannerStepDTO step : plannerDTO.getSteps()) {
	        if (step == null) {
	            continue;
	        }

	        step.setPlanner_id(plannerDTO.getPlanner_id());

	        int inserted = plannerDAO.insertPlannerStep(step);

	        if (inserted > 0) {
	            insertedStepCount++;
	        }
	    }

	    if (insertedStepCount == 0) {
	        result.put("success", false);
	        result.put("message", STEP_INSERT_FAIL_MESSAGE);
	        return result;
	    }

	    result.put("success", true);
	    result.put("message", "AI 플래너가 마이페이지에 저장되었습니다.");
	    result.put("planner_id", plannerDTO.getPlanner_id());
	    result.put("plannerId", plannerDTO.getPlanner_id());
	    result.put("planner_type", plannerDTO.getPlanner_type());
	    result.put("course_style", plannerDTO.getCourse_style());
	    result.put("weather_summary", plannerDTO.getWeather_summary());
	    result.put("route_notice", plannerDTO.getRoute_notice());
	    result.put("inserted_step_count", insertedStepCount);
	    result.put("steps", plannerDTO.getSteps());

	    return result;
	}

	/**
	 * 마이페이지 플래너 목록 조회
	 */
	public List<AIPlannerDTO> getMyPlanners(String memberId) {
		return plannerDAO.selectMyPlanners(memberId);
	}

	/**
	 * 플래너 상세 조회
	 */
	public HashMap<String, Object> getPlannerById(Long plannerId, String memberId) {

		AIPlannerDTO planner = plannerDAO.selectPlannerById(plannerId, memberId);

		if (planner == null) {
			return null;
		}

		List<AIPlannerStepDTO> steps = plannerDAO.selectPlannerSteps(plannerId);

		HashMap<String, Object> result = new HashMap<>();
		result.put("planner", planner);
		result.put("steps", steps);

		return result;
	}

	/**
	 * 플래너 삭제
	 */
	public int deletePlanner(Long plannerId, String memberId) {
		return plannerDAO.deletePlanner(plannerId, memberId);
	}

	/**
	 * Gemini 플래너 프롬프트 생성
	 */
	private String buildGeminiPlannerPrompt(
			AIPlannerDTO plannerDTO,
			FestivalDTO festival,
			String weatherSummary,
			List<NearbyPlaceDTO> nearbyFoods,
			List<NearbyPlaceDTO> nearbyTours,
			List<NearbyPlaceDTO> nearbyCultures
			) {
		StringBuilder sb = new StringBuilder();

		// ── 역할 정의 ──────────────────────────────────────────────
		sb.append("""
				너는 '축제로' 서비스의 AI 축제 여행 플래너 전문가다.
				사용자가 선택한 축제 정보, RAG 기반 유사도 추천 사유, 주변 장소 후보, 날씨 정보, 사용자 조건을 종합해
				최적의 하루 방문 코스를 JSON 배열로 설계한다.

				[절대 규칙]
				- 반드시 JSON 배열만 출력한다. 설명 문장, 마크다운, ```json 코드블록을 절대 포함하지 마라.
				- 실제 길찾기 경로, 실제 이동 시간, 출발지 기반 루트 계산을 하지 않는다.
				- 반드시 제공된 [선택한 축제]와 [주변 장소 후보]에 있는 장소만 선택한다.
				- 존재하지 않는 장소를 임의로 생성하지 마라.

				""");

		// ── 사용자 조건 ────────────────────────────────────────────
		String companionType = stringValueOrEmpty(plannerDTO.getCompanion_type());
		String courseStyle   = stringValueOrEmpty(plannerDTO.getCourse_style());
		String userInput     = stringValueOrEmpty(plannerDTO.getUser_input());
		String ragQuery      = stringValueOrEmpty(plannerDTO.getRag_query());
		String ragReason     = stringValueOrEmpty(plannerDTO.getRecommendation_reason());

		sb.append("[사용자 조건]\n");
		sb.append("- 방문일: ").append(plannerDTO.getVisit_date()).append("\n");
		sb.append("- 인원 수: ").append(plannerDTO.getPeople_count()).append("명\n");
		sb.append("- 동행 유형: ").append(companionType).append("\n");
		sb.append("- 코스 스타일: ").append(courseStyle).append("\n");
		sb.append("- 사용자 추가 요청: ").append(isBlank(userInput) ? "없음" : userInput).append("\n\n");

		// ── RAG 컨텍스트 ───────────────────────────────────────────
		sb.append("[RAG 기반 추천 컨텍스트] ← 이 정보를 reason/description 작성에 반드시 반영하라\n");
		sb.append("- 사용자 검색 의도: ").append(isBlank(ragQuery) ? "없음" : ragQuery).append("\n");
		sb.append("- 이 축제가 추천된 이유 (임베딩 유사도 기반): ")
		.append(isBlank(ragReason) ? "없음" : ragReason).append("\n");
		sb.append("""
				- 위 RAG 추천 사유를 각 step의 reason 필드에 자연스럽게 녹여 작성하라.
				  예) 사용자가 '가족과 체험 활동'을 검색했고 RAG 사유가 '체험 프로그램 다양'이면
				      reason에 "가족과 함께 체험 프로그램을 즐기기 좋은 축제입니다."처럼 작성한다.
				- description은 단순 장소 설명이 아닌, 사용자 조건(동행 유형·인원·스타일)에 맞춘 방문 팁을 포함한다.

				""");

		// ── 축제 정보 ──────────────────────────────────────────────
		sb.append("[선택한 축제]\n");
		sb.append(toJsonSafe(buildFestivalPromptMap(festival))).append("\n\n");

		// ── 날씨 정보 + 장소 선택 가이드 ──────────────────────────
		sb.append("[방문일 날씨]\n");
		sb.append(stringValueOrEmpty(weatherSummary)).append("\n");
		sb.append("""
				[날씨 기반 장소 선택 가이드]
				- 비·흐림·강풍: 실내 문화시설·식당 비중을 높이고, 야외 관광지는 최소화한다.
				- 맑음·선선: 야외 관광지와 축제장을 충분히 활용한다.
				- 더위(25°C 이상): 이동 간 휴식 장소(카페·실내)를 중간에 배치한다.
				- 날씨 미제공 시: 실내외 균형 있게 구성한다.

				""");

		// ── 동행 유형별 코스 설계 기준 ────────────────────────────
		sb.append("[동행 유형별 코스 설계 기준]\n");
		sb.append(companionTypeGuide(companionType));
		sb.append("\n");

		// ── 코스 스타일별 기준 ────────────────────────────────────
		sb.append("[코스 스타일별 기준]\n");
		sb.append(courseStyleGuide(courseStyle));
		sb.append("\n");

		// ── 주변 장소 후보 ─────────────────────────────────────────
		sb.append("[주변 음식점 후보] (최대 8개)\n");
		sb.append(toJsonSafe(buildNearbyPromptList(nearbyFoods, 8))).append("\n\n");

		sb.append("[주변 관광지 후보] (최대 8개)\n");
		sb.append(toJsonSafe(buildNearbyPromptList(nearbyTours, 8))).append("\n\n");

		sb.append("[주변 문화시설 후보] (최대 8개)\n");
		sb.append(toJsonSafe(buildNearbyPromptList(nearbyCultures, 8))).append("\n\n");

		// ── 출력 규칙 ──────────────────────────────────────────────
		sb.append("""
				[출력 규칙]
				1. JSON 최상위는 배열이다.
				2. step은 3~5개 생성한다.
				3. 첫 번째 step은 반드시 선택한 축제장이다 (type: FESTIVAL).
				4. 이후 step은 주변 장소 후보 목록에 있는 장소만 선택한다.
				5. 음식점(FOOD)은 점심 또는 저녁 타이밍에 자연스럽게 1~2개 배치한다.
				6. 실제 이동시간·경로를 언급하지 않는다.
				7. type은 FESTIVAL / FOOD / TOUR / CULTURE / REST 중 하나다.
				8. x·y 좌표는 반드시 문자열로 출력한다.
				9. source_api는 FESTIVAL_DB 또는 TOUR_API를 사용한다.
				10. kakao_place_url이 없으면 null로 출력한다.

				[각 step의 description 작성 기준]
				- 단순 "관광지입니다" 수준 금지. 아래 요소를 1~2문장으로 결합한다:
				  ① 이 장소의 핵심 매력 포인트
				  ② 동행 유형(가족·커플·친구 등)에 맞는 방문 팁
				  ③ 날씨·계절 정보가 있으면 연계 멘트 추가
				- 예시(가족·맑음): "체험 부스와 먹거리 행사가 풍성해 아이들과 함께 즐기기 좋습니다. 맑은 날씨 덕분에 야외 행사를 충분히 즐길 수 있습니다."

				[각 step의 reason 작성 기준]
				- RAG 추천 사유와 사용자 검색 의도를 연결해 '왜 이 장소인가'를 1문장으로 설명한다.
				- "AI가 추천했습니다" 같은 무의미한 문장 금지.
				- 예시: "사용자가 찾는 '가족 체험 여행' 키워드와 가장 유사도가 높은 장소로, 다양한 체험 프로그램을 제공합니다."

				[time_label 작성 기준]
				- 방문 순서와 시간대를 반영한 레이블 사용.
				- 예: "오전 첫 방문", "점심 식사", "오후 관광", "마지막 코스"

				""");

		// ── JSON 스키마 ────────────────────────────────────────────
		sb.append("""
				[JSON 배열 필드 — 모든 필드 필수 포함]
				step_order, time_label, title, description, type,
				place_name, address, x, y, reason, kakao_place_url,
				source_content_id, content_type_id, distance, first_image, source_api

				[출력 예시]
				[
				  {
				    "step_order": 1,
				    "time_label": "오전 첫 방문",
				    "title": "○○ 축제 방문",
				    "description": "가족과 함께 체험 부스와 공연을 즐길 수 있는 축제입니다. 맑은 날씨에 야외 행사가 더욱 풍성합니다.",
				    "type": "FESTIVAL",
				    "place_name": "○○ 축제",
				    "address": "○○시 ○○구",
				    "x": "127.0000",
				    "y": "37.0000",
				    "reason": "사용자가 검색한 '가족 체험 여행'과 가장 유사도가 높은 축제로, 체험 프로그램이 다양합니다.",
				    "kakao_place_url": null,
				    "source_content_id": "12345",
				    "content_type_id": "15",
				    "distance": 0,
				    "first_image": "",
				    "source_api": "FESTIVAL_DB"
				  }
				]
				""");

		return sb.toString();
	}

	/**
	 * 동행 유형별 코스 설계 가이드 반환
	 */
	private String companionTypeGuide(String companionType) {
		if (isBlank(companionType)) {
			return "- 동행 유형 정보 없음: 무난하게 관광지·음식점·문화시설을 균형 있게 구성한다.\n";
		}
		return switch (companionType.trim()) {
		case "가족", "FAMILY" -> """
				- 아이 동반 가능 여부를 우선 고려한다.
				- 체험형·교육형 관광지와 문화시설을 우선 배치한다.
				- 이동 피로를 줄이기 위해 축제장 근거리 장소를 선호한다.
				- 식사 장소는 단체석·가족석이 있을 법한 곳을 선택한다.
				""";
		case "커플", "COUPLE" -> """
				- 분위기 있는 관광지·카페·문화시설을 우선 배치한다.
				- 사진 찍기 좋은 포인트가 있는 장소를 선호한다.
				- 코스 전체가 자연스러운 데이트 동선이 되도록 구성한다.
				""";
		case "친구", "FRIENDS" -> """
				- 활동적이고 즐길 거리가 많은 장소를 우선 배치한다.
				- 맛집·먹거리 중심의 음식점을 적극 포함한다.
				- 이색 체험·유흥거리가 있는 문화시설도 고려한다.
				""";
		case "혼자", "SOLO" -> """
				- 개인이 여유롭게 탐방하기 좋은 관광지·문화시설을 선택한다.
				- 혼밥하기 편한 음식점을 배치한다.
				- 무리한 이동 없이 1인이 충분히 즐길 수 있는 코스로 구성한다.
				""";
				default -> "- 동행 유형(" + companionType + ")에 맞는 무난한 코스로 구성한다.\n";
		};
	}

	/**
	 * 코스 스타일별 설계 가이드 반환
	 */
	private String courseStyleGuide(String courseStyle) {
		if (isBlank(courseStyle)) {
			return "- 코스 스타일 정보 없음: RELAXED(여유형) 기본값으로 구성한다.\n";
		}
		return switch (courseStyle.trim().toUpperCase()) {
		case "RELAXED" -> """
				- 이동 부담 없이 축제장 인근 2~3곳만 선택한다.
				- 관광지보다 카페·음식점·문화시설 위주로 여유로운 코스를 구성한다.
				- step 수는 3~4개로 간결하게 유지한다.
				""";
		case "ACTIVE" -> """
				- 이동 거리가 조금 있더라도 방문 가치 높은 장소를 포함한다.
				- 관광지 2개 이상을 포함해 활동적인 코스를 구성한다.
				- step 수는 4~5개로 충실하게 구성한다.
				""";
		case "FOCUSED" -> """
				- 축제장 중심으로 주변 반경 내 핵심 1~2개 장소만 추가한다.
				- 이동 최소화, 축제 집중형 코스다.
				- step 수는 3개로 간결하게 유지한다.
				""";
				default -> "- 코스 스타일(" + courseStyle + ")에 맞는 적절한 코스로 구성한다.\n";
		};
	}

	/**
	 * festival 정보를 Gemini 프롬프트용 Map으로 변환
	 */
	private HashMap<String, Object> buildFestivalPromptMap(FestivalDTO festival) {
		HashMap<String, Object> map = new HashMap<>();

		map.put("content_id", festival.getContent_id());
		map.put("title", stringValueOrEmpty(festival.getTitle()));
		map.put("addr1", stringValueOrEmpty(festival.getAddr1()));
		map.put("map_x", festival.getMap_x());
		map.put("map_y", festival.getMap_y());
		map.put("event_start_date", stringValueOrEmpty(festival.getEvent_start_date()));
		map.put("event_end_date", stringValueOrEmpty(festival.getEvent_end_date()));
		map.put("first_image", stringValueOrEmpty(festival.getFirst_image()));
		map.put("overview", stringValueOrEmpty(festival.getOverview()));

		return map;
	}

	/**
	 * 주변 장소 후보를 Gemini 프롬프트용 List로 변환
	 */
	private List<HashMap<String, Object>> buildNearbyPromptList(List<NearbyPlaceDTO> places, int limit) {
		List<HashMap<String, Object>> result = new ArrayList<>();

		if (places == null || places.isEmpty()) {
			return result;
		}

		int count = 0;

		for (NearbyPlaceDTO place : places) {
			if (place == null) {
				continue;
			}

			if (place.getMapx() == 0.0 || place.getMapy() == 0.0) {
				continue;
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("source_content_id", stringValueOrEmpty(place.getContentid()));
			map.put("content_type_id", stringValueOrEmpty(place.getContenttypeid()));
			map.put("title", stringValueOrEmpty(place.getTitle()));
			map.put("place_name", stringValueOrEmpty(place.getTitle()));
			map.put("address", stringValueOrEmpty(place.getAddr1()));
			map.put("x", String.valueOf(place.getMapx()));
			map.put("y", String.valueOf(place.getMapy()));
			map.put("distance", place.getDist());
			map.put("first_image", stringValueOrEmpty(place.getFirstimage()));
			map.put("source_api", "TOUR_API");

			result.add(map);
			count++;

			if (count >= limit) {
				break;
			}
		}

		return result;
	}

	/**
	 * Gemini가 준 step 값 보정
	 */
	private void normalizeGeminiSteps(List<AIPlannerStepDTO> steps, FestivalDTO festival) {
		int order = 1;

		for (AIPlannerStepDTO step : steps) {
			if (step == null) {
				continue;
			}

			if (step.getStep_order() == null || step.getStep_order() <= 0) {
				step.setStep_order(order);
			}

			if (isBlank(step.getTime_label())) {
				step.setTime_label("추천 " + order);
			}

			if (isBlank(step.getTitle())) {
				step.setTitle("추천 장소");
			}

			if (isBlank(step.getType())) {
				step.setType("REST");
			}

			if (isBlank(step.getPlace_name())) {
				step.setPlace_name(step.getTitle());
			}

			if (isBlank(step.getDescription())) {
				step.setDescription("AI가 사용자 조건과 날씨를 반영해 추천한 장소입니다.");
			}

			if (isBlank(step.getReason())) {
				step.setReason("사용자 조건과 축제장 주변 정보를 반영했습니다.");
			}

			if (isBlank(step.getSource_api())) {
				step.setSource_api("TOUR_API");
			}

			// 첫 번째 step은 축제장 정보로 보정
			if (order == 1) {
				step.setType("FESTIVAL");
				step.setPlace_name(festival.getTitle());
				step.setTitle(festival.getTitle());
				step.setAddress(festival.getAddr1());
				step.setX(String.valueOf(festival.getMap_x()));
				step.setY(String.valueOf(festival.getMap_y()));
				step.setSource_content_id(String.valueOf(festival.getContent_id()));
				step.setContent_type_id("15");
				step.setDistance(0.0);
				step.setFirst_image(festival.getFirst_image());
				step.setSource_api("FESTIVAL_DB");
			}

			order++;
		}
	}

	/**
	 * TourAPI 주변 장소 안전 조회
	 */
	private List<NearbyPlaceDTO> getNearbyPlacesSafe(
			Double lat,
			Double lng,
			Integer radius,
			String contentTypeId
			) {
		try {
			List<NearbyPlaceDTO> list = festivalService.getNearbyPlaces(
					lat,
					lng,
					radius,
					contentTypeId
					);

			if (list == null) {
				return new ArrayList<>();
			}

			return list;

		} catch (Exception e) {
			System.out.println("주변 장소 조회 실패 contentTypeId = " + contentTypeId);
			System.out.println("error = " + e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * 날씨 조회 안전 호출
	 */
	private String getWeatherSummarySafe(LocalDate visitDate, Double mapX, Double mapY, String addr1) {
		try {
			if (visitDate == null || mapX == null || mapY == null) {
				return WEATHER_FAIL_MESSAGE;
			}

			return weatherService.getWeatherSummary(
					visitDate,
					mapX,
					mapY,
					addr1
					);

		} catch (Exception e) {
			System.out.println("날씨 조회 실패 = " + e.getMessage());
			return WEATHER_FAIL_MESSAGE;
		}
	}

	/**
	 * Gemini 응답에서 JSON 배열만 추출
	 */
	private String extractJsonArray(String geminiResponse) {
		if (geminiResponse == null) {
			return null;
		}

		String response = geminiResponse.trim();

		if (response.isEmpty()) {
			return null;
		}

		int jsonFenceStart = response.toLowerCase().indexOf("```json");
		if (jsonFenceStart >= 0) {
			int contentStart = jsonFenceStart + 7;
			int jsonFenceEnd = response.indexOf("```", contentStart);

			if (jsonFenceEnd > contentStart) {
				String fenced = response.substring(contentStart, jsonFenceEnd).trim();

				if (fenced.startsWith("[") && fenced.endsWith("]")) {
					return fenced;
				}
			}
		}

		int normalFenceStart = response.indexOf("```");
		if (normalFenceStart >= 0) {
			int contentStart = normalFenceStart + 3;
			int fenceEnd = response.indexOf("```", contentStart);

			if (fenceEnd > contentStart) {
				String fenced = response.substring(contentStart, fenceEnd).trim();

				if (fenced.startsWith("[") && fenced.endsWith("]")) {
					return fenced;
				}
			}
		}

		int first = response.indexOf("[");
		int last = response.lastIndexOf("]");

		if (first >= 0 && last > first) {
			String candidate = response.substring(first, last + 1).trim();

			if (candidate.startsWith("[") && candidate.endsWith("]")) {
				return candidate;
			}
		}

		return null;
	}

	private String toJsonSafe(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			return stringValueOrEmpty(value);
		}
	}

	private String stringValueOrEmpty(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	/**
	 * 요청값 검증
	 */
	private HashMap<String, Object> validatePlannerInput(AIPlannerDTO plannerDTO) {
		HashMap<String, Object> result = new HashMap<>();

		if (plannerDTO == null) {
			result.put("success", false);
			result.put("message", "planner 정보가 없습니다.");
			return result;
		}

		if (plannerDTO.getContent_id() == null) {
			result.put("success", false);
			result.put("message", "content_id는 필수입니다.");
			return result;
		}

		if (plannerDTO.getVisit_date() == null) {
			result.put("success", false);
			result.put("message", "방문 날짜는 필수입니다.");
			return result;
		}

		return null;
	}

	/**
	 * 축제 기간 검증
	 */
	private HashMap<String, Object> validateFestivalPeriod(
			LocalDate visitDate,
			String startDateText,
			String endDateText
			) {
		HashMap<String, Object> result = new HashMap<>();

		if (isBlank(startDateText) || isBlank(endDateText)) {
			result.put("success", false);
			result.put("message", "축제 기간 정보를 확인할 수 없습니다.");
			return result;
		}

		LocalDate startDate = parseDate(startDateText);
		LocalDate endDate = parseDate(endDateText);

		if (startDate == null || endDate == null) {
			result.put("success", false);
			result.put("message", "축제 기간 정보를 확인할 수 없습니다.");
			return result;
		}

		if (visitDate.isBefore(startDate) || visitDate.isAfter(endDate)) {
			result.put("success", false);
			result.put("message", "선택한 방문 날짜는 축제 기간에 포함되지 않습니다.");
			return result;
		}

		return null;
	}

	/**
	 * yyyyMMdd / yyyy-MM-dd 둘 다 처리
	 */
	private LocalDate parseDate(String dateText) {
		if (isBlank(dateText)) {
			return null;
		}

		DateTimeFormatter[] patterns = {
				DateTimeFormatter.ofPattern("yyyyMMdd"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd")
		};

		for (DateTimeFormatter pattern : patterns) {
			try {
				return LocalDate.parse(dateText.trim(), pattern);
			} catch (DateTimeParseException e) {
				// 다음 포맷 시도
			}
		}

		return null;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}