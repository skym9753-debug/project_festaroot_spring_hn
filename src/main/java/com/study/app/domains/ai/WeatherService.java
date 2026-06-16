package com.study.app.domains.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    /**
     * APIhub 기상청 단기예보 조회 URL
     *
     * 단기예보는 nx, ny 격자 좌표를 사용한다.
     */
    private static final String SHORT_FORECAST_URL =
            "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst";

    /**
     * APIhub 기상청 중기 육상예보 조회 URL
     *
     * 중기예보는 nx, ny가 아니라 reg 예보구역코드를 사용한다.
     */
    private static final String MID_LAND_FORECAST_URL =
            "https://apihub.kma.go.kr/api/typ01/url/fct_afs_wl.php";

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final RestTemplate restTemplate;

    /**
     * application.properties 예시:
     *
     * weather.service-key=APIhub에서_발급받은_authKey
     */
    @Value("${weather.service-key}")
    private String serviceKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 기존 코드 호환용 메서드
     *
     * 기존 PlannerService가 아직 addr1을 넘기지 않는 상태여도 컴파일이 깨지지 않게 유지한다.
     * 단, 중기예보는 주소 기반 reg 코드가 필요하므로 이 메서드로는 4~10일 후 중기예보를 정확히 조회할 수 없다.
     */
    public String getWeatherSummary(LocalDate visitDate, double longitude, double latitude) {
        return getWeatherSummary(visitDate, longitude, latitude, null);
    }

    /**
     * 방문일 날씨 요약 조회
     *
     * 기준:
     * - 오늘 ~ 3일 후: 단기예보 조회
     * - 4일 후 ~ 10일 후: 중기예보 조회
     * - 과거 날짜: 조회 불가
     * - 11일 이후: 예보 범위 밖
     *
     * @param visitDate 방문일
     * @param longitude 축제 경도
     * @param latitude 축제 위도
     * @param addr1 축제 주소
     * @return 날씨 요약 문장
     */
    public String getWeatherSummary(
            LocalDate visitDate,
            double longitude,
            double latitude,
            String addr1
    ) {
        if (visitDate == null) {
            return "방문일이 없어 날씨 정보를 조회하지 않았습니다.";
        }

        LocalDate today = LocalDate.now(KOREA_ZONE);
        long days = ChronoUnit.DAYS.between(today, visitDate);

        if (days < 0) {
            return "이미 지난 날짜라 날씨 정보는 반영하지 않았습니다.";
        }

        if (days <= 3) {
            return getShortTermWeatherSummary(visitDate, longitude, latitude);
        }

        if (days <= 10) {
            return getMidTermWeatherSummary(visitDate, addr1);
        }

        return "방문일이 예보 조회 가능 기간을 벗어나 날씨 정보는 반영하지 않았습니다.";
    }

    /**
     * 단기예보 조회
     *
     * 기존 getWeatherSummary 안에 있던 단기예보 로직을 분리한 메서드.
     */
    private String getShortTermWeatherSummary(
            LocalDate visitDate,
            double longitude,
            double latitude
    ) {
        try {
            /*
             * 축제 DB에는 위도/경도가 저장되어 있다.
             * 하지만 기상청 단기예보 API는 nx, ny 격자 좌표를 요구하므로 변환이 필요하다.
             */
            int[] grid = WeatherGridConverter.convert(longitude, latitude);

            ForecastBaseTime base = resolveShortTermBaseTime();

            String url = UriComponentsBuilder.fromUriString(SHORT_FORECAST_URL)
                    .queryParam("authKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", base.baseDate)
                    .queryParam("base_time", base.baseTime)
                    .queryParam("nx", grid[0])
                    .queryParam("ny", grid[1])
                    .build()
                    .toUriString();


            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            Map body = response.getBody();

            String resultCode = extractResultCode(body);
            String resultMsg = extractResultMsg(body);


            if (!"00".equals(resultCode)) {
                return "단기예보 API 호출 실패: " + resultMsg;
            }

            List<Map<String, Object>> items = extractItems(body);


            if (items.isEmpty()) {
                return "단기예보 API 응답에 예보 데이터가 없습니다.";
            }

            return buildShortTermSummary(items, visitDate);

        } catch (Exception e) {
            e.printStackTrace();
            return "단기예보 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 중기예보 조회
     *
     * 중기예보는 nx, ny가 아니라 reg 예보구역코드를 사용한다.
     * 현재는 축제 주소(addr1)를 기반으로 큰 권역 코드에 매핑한다.
     */
    private String getMidTermWeatherSummary(LocalDate visitDate, String addr1) {
        try {
            if (addr1 == null || addr1.trim().isEmpty()) {
                return "중기예보 조회를 위한 축제 주소 정보가 없어 날씨 정보는 반영하지 않았습니다.";
            }

            String reg = resolveMidLandRegCode(addr1);

            if (reg == null || reg.trim().isEmpty()) {
                return "중기예보 예보구역코드를 찾지 못해 날씨 정보는 반영하지 않았습니다.";
            }

            /*
             * 중기예보 발표자료는 typ01/url 계열이다.
             * disp=1을 주면 쉼표 구분 형태로 내려오므로 문자열로 받은 뒤 파싱한다.
             */
            String url = UriComponentsBuilder.fromUriString(MID_LAND_FORECAST_URL)
                    .queryParam("reg", reg)
                    .queryParam("tmfc", 0)
                    .queryParam("disp", 1)
                    .queryParam("authKey", serviceKey)
                    .build()
                    .toUriString();
            
            

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();



            if (body == null || body.trim().isEmpty()) {
                return "중기예보 API 응답이 비어 있습니다.";
            }

            if (body.contains("status") && body.contains("403")) {
                return "중기예보 API 권한이 없습니다. APIhub에서 중기예보자료 활용신청 상태를 확인해주세요.";
            }

            String summary = parseMidLandForecast(body, visitDate, reg);


            return summary;
            
            

        } catch (Exception e) {
            e.printStackTrace();
            return "중기예보 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 중기예보 응답 텍스트 파싱
     *
     * fct_afs_wl.php는 일반 JSON 응답이 아니라 typ01/url 형태의 텍스트 응답이다.
     * 환경에 따라 헤더/주석/공백이 섞일 수 있으므로 방어적으로 파싱한다.
     */
    private String buildMidTermSummaryFromText(String body, LocalDate visitDate, String reg) {
        String[] lines = body.split("\\r?\\n");

        String[] header = null;

        for (String line : lines) {
            String cleaned = cleanCsvLine(line);

            if (cleaned.isEmpty()) {
                continue;
            }

            /*
             * 헤더 라인 찾기
             * 예: REG_ID,TM_ST,TM_ED,...,WF,RN_ST,MIN,MAX,...
             */
            if (cleaned.contains("REG_ID") && cleaned.contains("TM_EF")) {
                header = splitCsv(cleaned);
                continue;
            }

            /*
             * 실제 데이터 라인 처리
             */
            if (header != null && cleaned.contains(",")) {
                String[] values = splitCsv(cleaned);

                Map<String, String> row = toRowMap(header, values);

                String rowReg = getIgnoreCase(row, "REG_ID");
                String tmEf = getIgnoreCase(row, "TM_EF");

                if (rowReg == null || tmEf == null) {
                    continue;
                }

                if (!reg.equals(rowReg.trim())) {
                    continue;
                }

                /*
                 * TM_EF는 보통 yyyyMMddHHmm 또는 yyyyMMdd 형태가 올 수 있다.
                 * 방문일 yyyyMMdd와 앞 8자리가 같으면 해당 날짜 예보로 판단한다.
                 */
                String visitDateText = visitDate.format(DateTimeFormatter.BASIC_ISO_DATE);

                if (!tmEf.startsWith(visitDateText)) {
                    continue;
                }

                String wf = getIgnoreCase(row, "WF");
                String rnSt = getIgnoreCase(row, "RN_ST");
                String min = getIgnoreCase(row, "MIN");
                String max = getIgnoreCase(row, "MAX");
                String conf = getIgnoreCase(row, "CONF");

                return buildMidTermSummary(wf, rnSt, min, max, conf);
            }
        }

        /*
         * 만약 헤더가 없는 응답이면 정확한 컬럼 매핑이 어렵다.
         * 이 경우에는 원문 일부를 로그로 확인한 뒤 파서 보정이 필요하다.
         */
        return "중기예보 API에서 방문일(" + visitDate + ")의 예보 데이터를 찾지 못했습니다.";
    }

    private String buildMidTermSummary(
            String wf,
            String rnSt,
            String min,
            String max,
            String conf
    ) {
        StringBuilder summary = new StringBuilder();
        summary.append("방문일 중기예보 기준 날씨: ");

        boolean hasAny = false;

        if (!isBlank(wf)) {
            summary.append(wf.trim());
            hasAny = true;
        }

        if (!isBlank(rnSt)) {
            if (hasAny) {
                summary.append(", ");
            }
            summary.append("강수확률 ").append(rnSt.trim()).append("%");
            hasAny = true;
        }

        if (!isBlank(min)) {
            if (hasAny) {
                summary.append(", ");
            }
            summary.append("최저 ").append(min.trim()).append("도");
            hasAny = true;
        }

        if (!isBlank(max)) {
            if (hasAny) {
                summary.append(", ");
            }
            summary.append("최고 ").append(max.trim()).append("도");
            hasAny = true;
        }

        if (!isBlank(conf)) {
            if (hasAny) {
                summary.append(", ");
            }
            summary.append("신뢰도 ").append(conf.trim());
            hasAny = true;
        }

        if (!hasAny) {
            return "중기예보 데이터는 조회됐지만 요약 가능한 날씨 값이 없습니다.";
        }

        return summary.toString();
    }
    /**
     * 중기 육상예보 응답 파싱
     *
     * 실제 응답 예시:
     * 11B00000,202606160600,202606201200,A02,109,2,WB04,WB00,없음,흐림,30,=
     *
     * 컬럼 순서:
     * 0 REG_ID
     * 1 TM_FC
     * 2 TM_EF
     * 3 MOD
     * 4 STN
     * 5 C
     * 6 SKY
     * 7 PRE
     * 8 CONF
     * 9 WF
     * 10 RN_ST
     */
    private String parseMidLandForecast(String body, LocalDate visitDate, String reg) {
        String visitDateText = visitDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String[] lines = body.split("\\r?\\n");

        String selectedWf = null;
        String selectedRnSt = null;
        String selectedConf = null;
        String selectedTime = null;

        for (String line : lines) {
            if (line == null) {
                continue;
            }

            String cleaned = line.trim();

            if (cleaned.isEmpty()) {
                continue;
            }

            // #START, #END, 헤더 라인은 제외
            if (cleaned.startsWith("#")) {
                continue;
            }

            String[] values = cleaned.split("\\s*,\\s*");

            if (values.length < 11) {
                continue;
            }

            String rowReg = values[0];
            String tmEf = values[2];

            if (!reg.equals(rowReg)) {
                continue;
            }

            if (!tmEf.startsWith(visitDateText)) {
                continue;
            }

            /*
             * 방문일에 00시/12시 데이터가 둘 다 있으면 12시 데이터를 우선 사용한다.
             */
            if (selectedTime == null || tmEf.endsWith("1200")) {
                selectedTime = tmEf;
                selectedConf = values[8];
                selectedWf = values[9];
                selectedRnSt = values[10];
            }

            if (tmEf.endsWith("1200")) {
                break;
            }
        }

        if (selectedTime == null) {
            return "중기예보 API에서 방문일(" + visitDate + ")의 예보 데이터를 찾지 못했습니다.";
        }

        return buildMidLandSummary(selectedWf, selectedRnSt, selectedConf);
    }

    /**
     * 중기 육상예보 요약 생성
     */
    private String buildMidLandSummary(String wf, String rnSt, String conf) {
        StringBuilder summary = new StringBuilder("방문일 중기예보 기준 날씨: ");

        boolean hasValue = false;

        if (!isBlank(wf)) {
            summary.append(wf.trim());
            hasValue = true;
        }

        if (!isBlank(rnSt)) {
            if (hasValue) {
                summary.append(", ");
            }
            summary.append("강수확률 ").append(rnSt.trim()).append("%");
            hasValue = true;
        }

        if (!isBlank(conf)) {
            if (hasValue) {
                summary.append(", ");
            }
            summary.append("신뢰도 ").append(conf.trim());
            hasValue = true;
        }

        if (!hasValue) {
            return "중기예보 데이터는 조회됐지만 요약 가능한 날씨 값이 없습니다.";
        }

        return summary.toString();
    }
    /**
     * 축제 주소를 중기 육상예보 reg 코드로 변환
     *
     * 우선은 시도 단위 매핑으로 처리한다.
     * 더 정밀하게 하려면 강원 영서/영동, 경북/경남 세부권역을 추가 분리하면 된다.
     */
    private String resolveMidLandRegCode(String addr1) {
        if (addr1 == null || addr1.trim().isEmpty()) {
            return null;
        }

        String region = addr1.trim();

 

        // 충북 / 충청북도
        if (region.contains("충북") || region.contains("충청북도")) {
            return "11C10000";
        }

        // 대전 / 세종 / 충남 / 충청남도
        if (
            region.contains("대전") ||
            region.contains("세종") ||
            region.contains("충남") ||
            region.contains("충청남도")
        ) {
            return "11C20000";
        }

        // 서울 / 인천 / 경기
        if (
            region.contains("서울") ||
            region.contains("인천") ||
            region.contains("경기")
        ) {
            return "11B00000";
        }

        // 강원
        if (region.contains("강원")) {
            return "11D10000";
        }

        // 전북 / 전라북도
        if (region.contains("전북") || region.contains("전라북도")) {
            return "11F10000";
        }

        // 광주 / 전남 / 전라남도
        if (
            region.contains("광주") ||
            region.contains("전남") ||
            region.contains("전라남도")
        ) {
            return "11F20000";
        }

        // 대구 / 경북 / 경상북도
        if (
            region.contains("대구") ||
            region.contains("경북") ||
            region.contains("경상북도")
        ) {
            return "11H10000";
        }

        // 부산 / 울산 / 경남 / 경상남도
        if (
            region.contains("부산") ||
            region.contains("울산") ||
            region.contains("경남") ||
            region.contains("경상남도")
        ) {
            return "11H20000";
        }

        // 제주
        if (region.contains("제주")) {
            return "11G00000";
        }

        return null;
    }
    /**
     * typ01/url 응답 라인 정리
     */
    private String cleanCsvLine(String line) {
        if (line == null) {
            return "";
        }

        String cleaned = line.trim();

        if (cleaned.isEmpty()) {
            return "";
        }

        /*
         * 주석 라인에서 #만 제거하고 헤더/데이터 후보로 사용한다.
         */
        while (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1).trim();
        }

        return cleaned;
    }

    private String[] splitCsv(String line) {
        return line.split("\\s*,\\s*");
    }

    private Map<String, String> toRowMap(String[] header, String[] values) {
        Map<String, String> row = new HashMap<>();

        int size = Math.min(header.length, values.length);

        for (int i = 0; i < size; i++) {
            if (header[i] == null) {
                continue;
            }

            row.put(header[i].trim().toUpperCase(), values[i] == null ? "" : values[i].trim());
        }

        return row;
    }

    private String getIgnoreCase(Map<String, String> row, String key) {
        if (row == null || key == null) {
            return null;
        }

        return row.get(key.toUpperCase());
    }

    /**
     * API 응답 resultCode 추출
     *
     * 단기예보 JSON 응답용
     */
    @SuppressWarnings("unchecked")
    private String extractResultCode(Map body) {

        if (body == null || body.get("response") == null) {
            return null;
        }

        Map<String, Object> response = (Map<String, Object>) body.get("response");
        Object headerObj = response.get("header");

        if (!(headerObj instanceof Map)) {
            return null;
        }

        Map<String, Object> header = (Map<String, Object>) headerObj;
        return String.valueOf(header.get("resultCode"));
    }

    /**
     * API 응답 resultMsg 추출
     *
     * 단기예보 JSON 응답용
     */
    @SuppressWarnings("unchecked")
    private String extractResultMsg(Map body) {

        if (body == null || body.get("response") == null) {
            return "응답 없음";
        }

        Map<String, Object> response = (Map<String, Object>) body.get("response");
        Object headerObj = response.get("header");

        if (!(headerObj instanceof Map)) {
            return "header 없음";
        }

        Map<String, Object> header = (Map<String, Object>) headerObj;
        return String.valueOf(header.get("resultMsg"));
    }

    /**
     * API 응답에서 item 배열 추출
     *
     * 단기예보 JSON 응답용
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map body) {

        if (body == null || body.get("response") == null) {
            return List.of();
        }

        Map<String, Object> response = (Map<String, Object>) body.get("response");

        Object bodyObj = response.get("body");
        if (!(bodyObj instanceof Map)) {
            return List.of();
        }

        Map<String, Object> responseBody = (Map<String, Object>) bodyObj;

        Object itemsObj = responseBody.get("items");
        if (!(itemsObj instanceof Map)) {
            return List.of();
        }

        Map<String, Object> items = (Map<String, Object>) itemsObj;

        Object itemObj = items.get("item");

        if (itemObj instanceof List) {
            return (List<Map<String, Object>>) itemObj;
        }

        if (itemObj instanceof Map) {
            return List.of((Map<String, Object>) itemObj);
        }

        return List.of();
    }

    /**
     * 방문일 정오 기준 단기예보 요약 생성
     */
    private String buildShortTermSummary(List<Map<String, Object>> items, LocalDate visitDate) {

        String visitDateText = visitDate.format(DateTimeFormatter.BASIC_ISO_DATE);

        Map<String, String> picked = new HashMap<>();

        for (Map<String, Object> item : items) {

            String fcstDate = String.valueOf(item.get("fcstDate"));
            String fcstTime = String.valueOf(item.get("fcstTime"));
            String category = String.valueOf(item.get("category"));

            if (!visitDateText.equals(fcstDate)) {
                continue;
            }

            /*
             * 현재는 방문일 12:00 기준으로 요약한다.
             * 필요하면 사용자가 선택한 start_time 기준으로 바꿀 수 있다.
             */
            if (!"1200".equals(fcstTime)) {
                continue;
            }

            if ("TMP".equals(category)
                    || "SKY".equals(category)
                    || "PTY".equals(category)
                    || "POP".equals(category)) {

                picked.put(category, String.valueOf(item.get("fcstValue")));
            }
        }

        if (picked.isEmpty()) {

            boolean hasVisitDateData = items.stream()
                    .anyMatch(item -> visitDateText.equals(String.valueOf(item.get("fcstDate"))));

            if (!hasVisitDateData) {
                return "단기예보 API에서 방문일(" + visitDate + ")의 예보 데이터를 찾지 못했습니다.";
            }

            return "단기예보 API에서 방문일(" + visitDate + ")의 정오 예보 데이터를 찾지 못했습니다.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("방문일 단기예보 정오 기준 날씨: ");

        if (picked.containsKey("TMP")) {
            summary.append("기온 ").append(picked.get("TMP")).append("도");
        }

        if (picked.containsKey("SKY")) {
            appendComma(summary);
            summary.append("하늘 ").append(convertSky(picked.get("SKY")));
        }

        if (picked.containsKey("PTY")) {
            appendComma(summary);
            summary.append("강수 ").append(convertPty(picked.get("PTY")));
        }

        if (picked.containsKey("POP")) {
            appendComma(summary);
            summary.append("강수확률 ").append(picked.get("POP")).append("%");
        }

        return summary.toString();
    }

    private void appendComma(StringBuilder summary) {
        if (!summary.toString().endsWith(": ")) {
            summary.append(", ");
        }
    }

    /**
     * SKY 코드 변환
     *
     * 1: 맑음
     * 3: 구름많음
     * 4: 흐림
     */
    private String convertSky(String value) {

        if ("1".equals(value)) {
            return "맑음";
        }

        if ("3".equals(value)) {
            return "구름많음";
        }

        if ("4".equals(value)) {
            return "흐림";
        }

        return value;
    }

    /**
     * PTY 코드 변환
     *
     * 0: 없음
     * 1: 비
     * 2: 비/눈
     * 3: 눈
     * 4: 소나기
     */
    private String convertPty(String value) {

        if ("0".equals(value)) {
            return "없음";
        }

        if ("1".equals(value)) {
            return "비";
        }

        if ("2".equals(value)) {
            return "비/눈";
        }

        if ("3".equals(value)) {
            return "눈";
        }

        if ("4".equals(value)) {
            return "소나기";
        }

        return value;
    }

    /**
     * 기상청 단기예보 base_date / base_time 계산
     *
     * 단기예보 발표 시각:
     * 02:00, 05:00, 08:00, 11:00, 14:00, 17:00, 20:00, 23:00
     *
     * 발표 직후에는 API 데이터가 아직 준비되지 않을 수 있으므로
     * 현재 시각에서 45분을 뺀 뒤 가장 최근 발표 시각을 선택한다.
     */
    private ForecastBaseTime resolveShortTermBaseTime() {

        LocalDateTime now = LocalDateTime.now(KOREA_ZONE).minusMinutes(45);

        int[] baseHours = {2, 5, 8, 11, 14, 17, 20, 23};

        LocalDate baseDate = now.toLocalDate();
        int selectedHour = 23;
        boolean found = false;

        for (int i = baseHours.length - 1; i >= 0; i--) {

            if (!now.toLocalTime().isBefore(LocalTime.of(baseHours[i], 0))) {
                selectedHour = baseHours[i];
                found = true;
                break;
            }
        }

        if (!found) {
            baseDate = baseDate.minusDays(1);
            selectedHour = 23;
        }

        ForecastBaseTime base = new ForecastBaseTime();
        base.baseDate = baseDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        base.baseTime = String.format("%02d00", selectedHour);

        return base;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class ForecastBaseTime {
        private String baseDate;
        private String baseTime;
    }
}