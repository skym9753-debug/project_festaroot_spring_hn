package com.study.app.domains.ai;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.ai.dto.AIPlannerDTO;
import com.study.app.domains.ai.dto.AIPlannerStepDTO;
import com.study.app.domains.festival.FestivalDAO;
import com.study.app.domains.festival.FestivalService;
import com.study.app.domains.festival.dto.FestivalDTO;
import com.study.app.domains.festival.dto.NearbyPlaceDTO;

@Service
public class PlannerService {

    private static final String PLANNER_TYPE = "FESTIVAL_COURSE";
    private static final String DEFAULT_COURSE_STYLE = "RELAXED";
    private static final String ROUTE_NOTICE =
            "지도 선은 실제 이동 경로가 아닌 추천 방문 순서입니다.";

    private static final String WEATHER_FAIL_MESSAGE =
            "방문일이 예보 범위를 벗어나거나 날씨 정보를 조회하지 못해 날씨는 반영하지 않았습니다.";

    private final PlannerDAO plannerDAO;
    private final FestivalDAO festivalDAO;
    private final FestivalService festivalService;
    private final WeatherService weatherService;

    public PlannerService(
            PlannerDAO plannerDAO,
            FestivalDAO festivalDAO,
            FestivalService festivalService,
            WeatherService weatherService
    ) {
        this.plannerDAO = plannerDAO;
        this.festivalDAO = festivalDAO;
        this.festivalService = festivalService;
        this.weatherService = weatherService;
    }

    /**
     * AI 축제 코스 추천 생성
     *
     * 현재 방향:
     * - 출발지 사용 안 함
     * - 카카오 Local API 사용 안 함
     * - 실제 이동시간/길찾기 계산 안 함
     * - 축제장 좌표 + TourAPI 주변 장소 + 날씨 요약 기반 코스 생성
     */
    @Transactional
    public HashMap<String, Object> createPlannerWithDummySteps(AIPlannerDTO plannerDTO, String memberId) {

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> validation = validatePlannerInput(plannerDTO);
        if (validation != null) {
            return validation;
        }

        FestivalDTO festival = festivalDAO.selectByContentId(String.valueOf(plannerDTO.getContent_id()));

        if (festival == null) {
            result.put("success", false);
            result.put("message", "축제 정보를 확인할 수 없습니다.");
            return result;
        }

        if (festival.getMap_x() == null || festival.getMap_y() == null) {
            result.put("success", false);
            result.put("message", "축제 좌표 정보가 없어 코스를 생성할 수 없습니다.");
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

        String weatherSummary = getWeatherSummarySafe(
                plannerDTO.getVisit_date(),
                festival.getMap_x(),
                festival.getMap_y()
        );

        // ai_planner 기본 정보 세팅
        plannerDTO.setMember_id(memberId);
        plannerDTO.setTitle(festival.getTitle() + " 주변 추천 코스");
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

        // TourAPI 주변 장소 조회
        // getNearbyPlaces(lat, lng, radius, contentTypeId)
        Double lat = festival.getMap_y(); // 위도
        Double lng = festival.getMap_x(); // 경도
        Integer radius = 3000;

        List<NearbyPlaceDTO> nearbyFoods = getNearbyPlacesSafe(lat, lng, radius, "39");    // 음식점
        List<NearbyPlaceDTO> nearbyTours = getNearbyPlacesSafe(lat, lng, radius, "12");    // 관광지
        List<NearbyPlaceDTO> nearbyCultures = getNearbyPlacesSafe(lat, lng, radius, "14"); // 문화시설

        // ai_planner 저장
        int plannerResult = plannerDAO.insertPlanner(plannerDTO);

        if (plannerResult == 0 || plannerDTO.getPlanner_id() == null) {
            result.put("success", false);
            result.put("message", "planner 저장에 실패했습니다.");
            return result;
        }

        // 추천 코스 step 생성
        List<AIPlannerStepDTO> steps = buildCourseSteps(
                plannerDTO,
                festival,
                nearbyFoods,
                nearbyTours,
                nearbyCultures,
                weatherSummary
        );

        int insertedStepCount = 0;

        for (AIPlannerStepDTO step : steps) {
            step.setPlanner_id(plannerDTO.getPlanner_id());

            int inserted = plannerDAO.insertPlannerStep(step);
            if (inserted > 0) {
                insertedStepCount++;
            }
        }

        if (insertedStepCount != steps.size()) {
            result.put("success", false);
            result.put("message", "추천 코스 저장에 실패했습니다.");
            return result;
        }

        result.put("success", true);
        result.put("message", "AI 축제 코스가 생성되었습니다.");
        result.put("planner_id", plannerDTO.getPlanner_id());
        result.put("planner_type", plannerDTO.getPlanner_type());
        result.put("course_style", plannerDTO.getCourse_style());
        result.put("weather_summary", weatherSummary);
        result.put("route_notice", plannerDTO.getRoute_notice());
        result.put("steps", steps);

        result.put("nearbyFoods", nearbyFoods);
        result.put("nearbyTours", nearbyTours);
        result.put("nearbyCultures", nearbyCultures);

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
     * 추천 코스 step 구성
     */
    private List<AIPlannerStepDTO> buildCourseSteps(
            AIPlannerDTO plannerDTO,
            FestivalDTO festival,
            List<NearbyPlaceDTO> nearbyFoods,
            List<NearbyPlaceDTO> nearbyTours,
            List<NearbyPlaceDTO> nearbyCultures,
            String weatherSummary
    ) {
        List<AIPlannerStepDTO> steps = new ArrayList<>();

        boolean badWeather = isBadWeather(weatherSummary);

        // STEP 1. 축제장 방문
        AIPlannerStepDTO festivalStep = new AIPlannerStepDTO();
        festivalStep.setStep_order(1);
        festivalStep.setTime_label("축제 방문");
        festivalStep.setTitle(festival.getTitle());
        festivalStep.setDescription("선택한 축제를 중심 일정으로 구성했습니다.");
        festivalStep.setType("FESTIVAL");
        festivalStep.setPlace_name(festival.getTitle());
        festivalStep.setAddress(festival.getAddr1());
        festivalStep.setX(String.valueOf(festival.getMap_x()));
        festivalStep.setY(String.valueOf(festival.getMap_y()));
        festivalStep.setReason("사용자가 선택한 추천 축제입니다.");
        festivalStep.setSource_content_id(String.valueOf(festival.getContent_id()));
        festivalStep.setContent_type_id("15");
        festivalStep.setDistance(0.0);
        festivalStep.setFirst_image(festival.getFirst_image());
        festivalStep.setSource_api("FESTIVAL_DB");
        steps.add(festivalStep);

        // STEP 2. 음식점 추천
        NearbyPlaceDTO food = getFirstValidPlace(nearbyFoods);

        if (food != null) {
            steps.add(createNearbyStep(
                    2,
                    "식사 추천",
                    food.getTitle(),
                    "축제장 주변 음식점 중 함께 들르기 좋은 장소입니다.",
                    "FOOD",
                    food,
                    "축제 관람 전후로 식사하기 좋은 주변 음식점입니다."
            ));
        } else {
            steps.add(createManualStep(
                    2,
                    "식사 추천",
                    "주변 식사 또는 휴식",
                    "주변 음식점 정보가 부족하여 축제장 주변에서 자유롭게 식사 또는 휴식하는 코스로 구성했습니다.",
                    "REST",
                    festival,
                    "주변 음식점 후보가 부족하여 수동 휴식 코스로 구성했습니다."
            ));
        }

        // STEP 3. 날씨 기반 주변 코스
        if (badWeather) {
            NearbyPlaceDTO culture = getFirstValidPlace(nearbyCultures);

            if (culture != null) {
                steps.add(createNearbyStep(
                        3,
                        "실내 코스",
                        culture.getTitle(),
                        "비, 흐림 또는 강수 가능성을 고려해 실내 중심의 문화시설을 추천합니다.",
                        "CULTURE",
                        culture,
                        "날씨 영향을 덜 받는 실내형 코스로 구성했습니다."
                ));
            } else {
                NearbyPlaceDTO tour = getFirstValidPlace(nearbyTours);

                if (tour != null) {
                    steps.add(createNearbyStep(
                            3,
                            "주변 코스",
                            tour.getTitle(),
                            "문화시설 후보가 부족하여 축제장 근처 관광지를 추천합니다.",
                            "TOUR",
                            tour,
                            "축제장 주변에서 함께 둘러보기 좋은 장소입니다."
                    ));
                } else {
                    steps.add(createManualStep(
                            3,
                            "주변 코스",
                            "축제장 주변 자유 관람",
                            "주변 장소 정보가 부족하여 축제장 주변을 여유롭게 둘러보는 코스로 구성했습니다.",
                            "REST",
                            festival,
                            "주변 장소 후보가 부족하여 축제장 기준으로 구성했습니다."
                    ));
                }
            }
        } else {
            NearbyPlaceDTO tour = getFirstValidPlace(nearbyTours);

            if (tour != null) {
                steps.add(createNearbyStep(
                        3,
                        "주변 코스",
                        tour.getTitle(),
                        "날씨가 무난하여 축제장 근처 관광지를 함께 추천합니다.",
                        "TOUR",
                        tour,
                        "축제장 주변에서 함께 둘러보기 좋은 관광지입니다."
                ));
            } else {
                NearbyPlaceDTO culture = getFirstValidPlace(nearbyCultures);

                if (culture != null) {
                    steps.add(createNearbyStep(
                            3,
                            "문화 코스",
                            culture.getTitle(),
                            "관광지 후보가 부족하여 축제장 근처 문화시설을 추천합니다.",
                            "CULTURE",
                            culture,
                            "축제장 주변에서 함께 방문하기 좋은 문화시설입니다."
                    ));
                } else {
                    steps.add(createManualStep(
                            3,
                            "주변 코스",
                            "축제장 주변 자유 관람",
                            "주변 장소 정보가 부족하여 축제장 주변을 여유롭게 둘러보는 코스로 구성했습니다.",
                            "REST",
                            festival,
                            "주변 장소 후보가 부족하여 축제장 기준으로 구성했습니다."
                    ));
                }
            }
        }

        // STEP 4. 마무리 코스
        NearbyPlaceDTO culture = getFirstValidPlace(nearbyCultures);

        if (culture != null && !isAlreadyAdded(steps, culture.getContentid())) {
            steps.add(createNearbyStep(
                    4,
                    "마무리 코스",
                    culture.getTitle(),
                    "축제 방문 후 무리 없이 마무리하기 좋은 문화시설입니다.",
                    "CULTURE",
                    culture,
                    "여유롭게 마무리하기 좋은 주변 문화시설입니다."
            ));
        } else {
            steps.add(createManualStep(
                    4,
                    "마무리 코스",
                    "일정 마무리",
                    "축제 방문 후 여유롭게 일정을 마무리합니다.",
                    "REST",
                    festival,
                    "추천 코스를 마무리하는 단계입니다."
            ));
        }

        return steps;
    }

    /**
     * TourAPI 주변 장소 step 생성
     */
    private AIPlannerStepDTO createNearbyStep(
            int order,
            String timeLabel,
            String title,
            String description,
            String type,
            NearbyPlaceDTO place,
            String reason
    ) {
        AIPlannerStepDTO step = new AIPlannerStepDTO();

        step.setStep_order(order);
        step.setTime_label(timeLabel);
        step.setTitle(title);
        step.setDescription(description);
        step.setType(type);
        step.setPlace_name(place.getTitle());
        step.setAddress(place.getAddr1());
        step.setX(String.valueOf(place.getMapx()));
        step.setY(String.valueOf(place.getMapy()));
        step.setReason(reason);

        step.setSource_content_id(place.getContentid());
        step.setContent_type_id(place.getContenttypeid());
        step.setDistance(place.getDist());
        step.setFirst_image(place.getFirstimage());
        step.setSource_api("TOUR_API");

        return step;
    }

    /**
     * 주변 장소가 부족할 때 사용하는 수동 step
     */
    private AIPlannerStepDTO createManualStep(
            int order,
            String timeLabel,
            String title,
            String description,
            String type,
            FestivalDTO festival,
            String reason
    ) {
        AIPlannerStepDTO step = new AIPlannerStepDTO();

        step.setStep_order(order);
        step.setTime_label(timeLabel);
        step.setTitle(title);
        step.setDescription(description);
        step.setType(type);
        step.setPlace_name(title);
        step.setAddress(festival.getAddr1());
        step.setX(String.valueOf(festival.getMap_x()));
        step.setY(String.valueOf(festival.getMap_y()));
        step.setReason(reason);

        step.setSource_content_id(null);
        step.setContent_type_id(null);
        step.setDistance(null);
        step.setFirst_image(null);
        step.setSource_api("MANUAL");

        return step;
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
     * 좌표가 있는 첫 번째 장소 선택
     */
    private NearbyPlaceDTO getFirstValidPlace(List<NearbyPlaceDTO> places) {
        if (places == null || places.isEmpty()) {
            return null;
        }

        for (NearbyPlaceDTO place : places) {
            if (place == null) {
                continue;
            }

            if (place.getMapx() == 0.0 || place.getMapy() == 0.0) {
                continue;
            }

            return place;
        }

        return null;
    }

    /**
     * 중복 장소 방지
     */
    private boolean isAlreadyAdded(List<AIPlannerStepDTO> steps, String contentId) {
        if (steps == null || contentId == null) {
            return false;
        }

        for (AIPlannerStepDTO step : steps) {
            if (contentId.equals(step.getSource_content_id())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 날씨 조회 안전 호출
     */
    private String getWeatherSummarySafe(LocalDate visitDate, Double mapX, Double mapY) {
        try {
            if (visitDate == null || mapX == null || mapY == null) {
                return WEATHER_FAIL_MESSAGE;
            }

            return weatherService.getWeatherSummary(
                    visitDate,
                    mapX, // 경도
                    mapY  // 위도
            );

        } catch (Exception e) {
            System.out.println("날씨 조회 실패 = " + e.getMessage());
            return WEATHER_FAIL_MESSAGE;
        }
    }

    /**
     * 악천후 여부 판단
     */
    private boolean isBadWeather(String weatherSummary) {
        if (weatherSummary == null) {
            return false;
        }

        return weatherSummary.contains("비")
                || weatherSummary.contains("눈")
                || weatherSummary.contains("강수")
                || weatherSummary.contains("소나기")
                || weatherSummary.contains("흐림")
                || weatherSummary.contains("폭염")
                || weatherSummary.contains("강풍");
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