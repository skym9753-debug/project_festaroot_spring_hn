package com.study.app.domains.weather;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service("liveWeatherService")
public class WeatherService {

	@Value("${openweatehr.api.key}")
	private String apiKey;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, Object> getLiveWeather(String region) {
		Map<String, Object> result = new HashMap<>();
		String cityName = mapRegionToCity(region);

		try {
			String url = UriComponentsBuilder.fromUriString("https://api.openweathermap.org/data/2.5/weather")
					.queryParam("q", cityName)
                    .queryParam("appid", apiKey) // ⭕ 이제 정상적인 b3ffefb4... 키가 들어갑니다.
                    .queryParam("units", "metric")
					.queryParam("lang", "kr")
                    .toUriString();

			String response = restTemplate.getForObject(url, String.class);
			JsonNode root = objectMapper.readTree(response);

			double temp = root.path("main").path("temp").asDouble();
			int humidity = root.path("main").path("humidity").asInt();
			String description = root.path("weather").get(0).path("description").asText();
			String mainWeather = root.path("weather").get(0).path("main").asText().toUpperCase();

			result.put("temp", (int) Math.round(temp));
			result.put("humidity", humidity + "%");
			result.put("description", description);

			if (mainWeather.contains("RAIN") || mainWeather.contains("DRIZZLE")) {
				result.put("icon", "🌧️");
				result.put("rainProb", "80%");
			} else if (mainWeather.contains("SNOW")) {
				result.put("icon", "❄️");
				result.put("rainProb", "30%");
			} else if (mainWeather.contains("CLOUD")) {
				result.put("icon", "☁️");
				result.put("rainProb", "20%");
			} else {
				result.put("icon", "☀️");
				result.put("rainProb", "0%");
			}

			result.put("dust", "좋음");

			String comment = generateWeatherComment((int) Math.round(temp), mainWeather);
			result.put("comment", comment);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("날씨 연동 파이프라인 에러 : " + e.getMessage());
			result.put("temp", 22);
			result.put("humidity", "50%");
			result.put("icon", "☀️");
			result.put("rainProb", "10%");
			result.put("dust", "보통");
			result.put("comment", "날씨 정보를 불러올 수 없지만, 축제는 계속됩니다! 🎡");
		}

		return result;
	}

	private String mapRegionToCity(String region) {
		switch (region) {
		case "서울": return "Seoul";
		case "부산": return "Busan";
		case "제주": return "Jeju";
		case "인천": return "Incheon";
		case "대구": return "Daegu";
		case "대전": return "Daejeon";
		case "광주": return "Gwangju";
		case "울산": return "Ulsan";
		case "경기": return "Gyeonggi-do";
		case "강원": return "Gangwon-do";
		case "충북": return "Chungcheongbuk-do";
		case "충남": return "Chungcheongnam-do";
		case "전북": return "Jeollabuk-do";
		case "전남": return "Jeollanam-do";
		case "경북": return "Gyeongsangbuk-do";
		case "경남": return "Gyeongsangnam-do";
		default: return "Seoul";
		}
	}

	private String generateWeatherComment(int temp, String mainWeather) {
		if (mainWeather.contains("RAIN") || mainWeather.contains("DRIZZLE")) {
			return "비가 오고 있어요! 🌧️ 안전을 위해 우산을 꼭 챙기세요.";
		}
		if (mainWeather.contains("SNOW")) {
			return "눈이 내려요! ❄️  빙판길 조심하시고 따뜻하게 입으세요.";
		}
		if (temp >= 28) {
			return "오늘은 햇빛이 무척 뜨거워요! ☀️  자외선 차단제와 양산을 준비하세요.";
		}
		if (temp <= 12) {
			return "날씨가 많이 쌀쌀해요! 🍂  두터운 외투를 걸치고 축제를 즐기세요.";
		}
		return "축제 가기 딱 좋은 기분 좋은 날씨예요! 🎡  행복한 하루 보내세요.";
	}
}