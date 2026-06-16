package com.study.app.domains.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final String FORECAST_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private static final String WEATHER_SKIP_MESSAGE =
            "방문일이 예보 범위를 벗어나 날씨 정보는 반영하지 않았습니다.";

    private final RestTemplate restTemplate;

    @Value("${weather.service-key}")
    private String serviceKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeatherSummary(LocalDate visitDate, double longitude, double latitude) {
        if (visitDate == null || isOutOfForecastRange(visitDate)) {
            return WEATHER_SKIP_MESSAGE;
        }

        try {
            int[] grid = WeatherGridConverter.convert(longitude, latitude);
            ForecastBaseTime base = resolveBaseTime();

            String url = UriComponentsBuilder.fromUriString(FORECAST_URL)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", base.baseDate)
                    .queryParam("base_time", base.baseTime)
                    .queryParam("nx", grid[0])
                    .queryParam("ny", grid[1])
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            List<Map<String, Object>> items = extractItems(response.getBody());
            if (items.isEmpty()) {
                return WEATHER_SKIP_MESSAGE;
            }

            return buildSummary(items, visitDate);

        } catch (Exception e) {
            return WEATHER_SKIP_MESSAGE;
        }
    }

    private boolean isOutOfForecastRange(LocalDate visitDate) {
        LocalDate today = LocalDate.now();
        return visitDate.isBefore(today) || visitDate.isAfter(today.plusDays(3));
    }

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

    private String buildSummary(List<Map<String, Object>> items, LocalDate visitDate) {
        String visitDateText = visitDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, String> picked = new HashMap<>();

        for (Map<String, Object> item : items) {
            String fcstDate = String.valueOf(item.get("fcstDate"));
            String fcstTime = String.valueOf(item.get("fcstTime"));
            String category = String.valueOf(item.get("category"));

            if (!visitDateText.equals(fcstDate) || !"1200".equals(fcstTime)) {
                continue;
            }

            if ("TMP".equals(category) || "SKY".equals(category) || "PTY".equals(category) || "POP".equals(category)) {
                picked.put(category, String.valueOf(item.get("fcstValue")));
            }
        }

        if (picked.isEmpty()) {
            return WEATHER_SKIP_MESSAGE;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("방문일 정오 기준 날씨: ");

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

    private ForecastBaseTime resolveBaseTime() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(45);
        int[] baseHours = { 2, 5, 8, 11, 14, 17, 20, 23 };

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
        }

        ForecastBaseTime base = new ForecastBaseTime();
        base.baseDate = baseDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        base.baseTime = String.format("%02d00", selectedHour);
        return base;
    }

    private static class ForecastBaseTime {
        private String baseDate;
        private String baseTime;
    }
}
