package com.study.app.domains.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoMapService {

    public static final String CATEGORY_FD6 = "FD6";
    public static final String CATEGORY_CE7 = "CE7";
    public static final String CATEGORY_AT4 = "AT4";
    public static final String CATEGORY_CT1 = "CT1";
    public static final String CATEGORY_PK6 = "PK6";

    private static final String KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final String CATEGORY_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final String START_SEARCH_FAIL_MESSAGE = "출발지를 찾을 수 없습니다. 역명 또는 장소명을 더 정확히 입력해주세요.";

    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    public KakaoMapService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HashMap<String, Object> searchDeparture(String keyword) {
        HashMap<String, Object> result = new HashMap<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", START_SEARCH_FAIL_MESSAGE);
            return result;
        }

        try {
            String url = UriComponentsBuilder.fromUriString(KEYWORD_SEARCH_URL)
                    .queryParam("query", keyword)
                    .queryParam("size", 1)
                    .build()
                    .encode()
                    .toUriString();

            HttpHeaders headers = createHeader();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || body.get("documents") == null) {
                result.put("success", false);
                result.put("message", START_SEARCH_FAIL_MESSAGE);
                return result;
            }

            List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
            if (docs.isEmpty()) {
                result.put("success", false);
                result.put("message", START_SEARCH_FAIL_MESSAGE);
                return result;
            }

            result.put("success", true);
            result.put("data", docs);
            return result;

        } catch (Exception e) {
            System.out.println("카카오 출발지 검색 실패 keyword = " + keyword);
            System.out.println("카카오 출발지 검색 실패 message = " + e.getMessage());
            e.printStackTrace();
            
            
            result.put("success", false);
            result.put("message", START_SEARCH_FAIL_MESSAGE);
            return result;
        }
    }

    public List<Map<String, Object>> searchAroundByCategory(String x, String y, String categoryCode) {
        try {
            String url = UriComponentsBuilder.fromUriString(CATEGORY_SEARCH_URL)
                    .queryParam("category_group_code", categoryCode)
                    .queryParam("x", x)
                    .queryParam("y", y)
                    .queryParam("radius", 2000)
                    .queryParam("size", 15)
                    .queryParam("sort", "distance")
                    .build()
                    .encode()
                    .toUriString();

            HttpHeaders headers = createHeader();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || body.get("documents") == null) {
                return List.of();
            }

            return (List<Map<String, Object>>) body.get("documents");

        } catch (Exception e) {
            return List.of();
        }
    }

    public HashMap<String, Object> searchAroundByAllCategories(String x, String y) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(CATEGORY_FD6, searchAroundByCategory(x, y, CATEGORY_FD6));
        result.put(CATEGORY_CE7, searchAroundByCategory(x, y, CATEGORY_CE7));
        result.put(CATEGORY_AT4, searchAroundByCategory(x, y, CATEGORY_AT4));
        result.put(CATEGORY_CT1, searchAroundByCategory(x, y, CATEGORY_CT1));
        result.put(CATEGORY_PK6, searchAroundByCategory(x, y, CATEGORY_PK6));
        return result;
    }

    private HttpHeaders createHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoClientId);
        return headers;
    }
}
