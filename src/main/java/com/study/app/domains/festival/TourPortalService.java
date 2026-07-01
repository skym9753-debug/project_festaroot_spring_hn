package com.study.app.domains.festival;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TourPortalService {
	
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TourPortalDAO tourPortalDAO;

    @Value("${tour.portal.api.url}")
    private String apiUrl;

    @Value("${tour.portal.api.key}")
    private String apiKey;

    @Value("${tour.portal.api.per-page:100}")
    private int perPage;

    @Transactional
    public String syncTourPortal() throws Exception {
    	tourPortalDAO.deleteStaging();

        int page = 1;
        int fetchedCount = 0;
        int savedCount = 0;

        while (true) {
        	String url = apiUrl
        	        + "?page=" + page
        	        + "&perPage=" + perPage;

            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("Authorization", "Infuser " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");

            if (!data.isArray() || data.size() == 0) {
                break;
            }

            for (JsonNode item : data) {
                String sourceId = text(item, "고유 아이디");
                String regionCode = text(item, "지역코드");
                String sigunguCode = text(item, "시구군코드");
                String tourismPortalUrl = normalizeUrl(text(item, "지역별 문화관광 홈페이지 주소"));

                if (isBlank(sourceId) || isBlank(regionCode) || isBlank(sigunguCode)) {
                    continue;
                }

                Map<String, Object> param = new HashMap<>();
                param.put("sourceId", sourceId);
                param.put("regionCode", regionCode);
                param.put("sigunguCode", sigunguCode);
                param.put("tourismPortalUrl", tourismPortalUrl);

                savedCount += tourPortalDAO.insertStaging(param);
            }

            fetchedCount += data.size();

            int totalCount = root.path("totalCount").asInt(0);

            if (totalCount > 0 && fetchedCount >= totalCount) {
                break;
            }

            page++;
        }

        int updatedCount = tourPortalDAO.updateRegionMaster();

        return "API 조회 " + fetchedCount
                + "건, STG 저장 " + savedCount
                + "건, REGION_MASTER 반영 " + updatedCount + "건";
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText().trim();
    }

    private String normalizeUrl(String url) {
        if (isBlank(url)) {
            return null;
        }

        String value = url.trim();

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        return "https://" + value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
