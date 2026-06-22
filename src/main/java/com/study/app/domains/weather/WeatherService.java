package com.study.app.domains.weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("liveWeatherService")
public class WeatherService {

	// 💡 application.properties에 등록된 기상청 service-key를 가져옵니다 (festa_root secret과 자동 연동)
	@Value("${weather.service-key}")
	private String apiKey;

	// 💡 기존 AI 서비스에서 사용하던 APIhub 기상청 단기예보 주소를 그대로 이식했습니다.
	private static final String SHORT_FORECAST_URL =
			"https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst";

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, Object> getLiveWeather(String region) {
		Map<String, Object> result = new HashMap<>();

		// 1. 국내 주요 17개 시·도 명칭을 기상청 격자 좌표 관측망(NX, NY)으로 변환
		int[] grid = mapRegionToGrid(region);
		int nx = grid[0];
		int ny = grid[1];

		try {
			// 2. 기존 AI 날씨 소스코드의 안전한 기상청 베이스 타임 연산 함수 엔진 호출
			ForecastBaseTime base = resolveShortTermBaseTime();

			// 3. APIhub 데이터 전송 규격 명세에 맞춰 동적 쿼리 파라미터 조합
			String url = UriComponentsBuilder.fromUriString(SHORT_FORECAST_URL)
					.queryParam("authKey", apiKey) // ⭕ APIhub 규격인 authKey 주입
					.queryParam("pageNo", 1)
					.queryParam("numOfRows", 200) // 현재 정시 시간대 데이터 캡처용 최적화 개수
					.queryParam("dataType", "JSON")
					.queryParam("base_date", base.baseDate)
					.queryParam("base_time", base.baseTime)
					.queryParam("nx", nx)
					.queryParam("ny", ny)
					.build()
					.toUriString();

			String response = restTemplate.getForObject(url, String.class);
			JsonNode root = objectMapper.readTree(response);
			JsonNode itemNode = root.path("response").path("body").path("items").path("item");

			// 시스템 가드용 기본 변수 초기화
			int temp = 22;
			int sky = 1;
			int pty = 0;
			String humidity = "55%";

			// 현재 시각 기준 정시 문자열 생성 (예: 오후 3시 14분 ➔ "1500")
			String currentHourTime = LocalTime.now(KOREA_ZONE).format(DateTimeFormatter.ofPattern("HH00"));

			if (itemNode.isArray()) {
				for (JsonNode item : itemNode) {
					String fcstTime = item.path("fcstTime").asText();
					String category = item.path("category").asText();
					String fcstValue = item.path("fcstValue").asText();

					// 수많은 하루 예보 데이터 중, 현재 정시 시간대 데이터 매칭 구역
					if (currentHourTime.equals(fcstTime)) {
						switch (category) {
							case "TMP": // 기온
								temp = Integer.parseInt(fcstValue);
								break;
							case "REH": // 습도
								humidity = fcstValue + "%";
								break;
							case "SKY": // 하늘상태 (1:맑음, 3:구름많음, 4:흐림)
								sky = Integer.parseInt(fcstValue);
								break;
							case "PTY": // 강수형태 (0:없음, 1:비, 2:비/눈, 3:눈, 4:소나기)
								pty = Integer.parseInt(fcstValue);
								break;
						}
					}
				}
			}

			// 4. 기상청 코드값을 리액트 화면 구조(이모지, 한글 설명)에 맞게 맵 매핑 변환
			String description = "맑음";
			String icon = "☀️";
			String rainProb = "0%";

			if (pty > 0) { // 비나 눈이 올 때 우선 판별
				if (pty == 1 || pty == 4) { description = "비/소나기"; icon = "🌧️"; rainProb = "80%"; }
				else if (pty == 3) { description = "눈이 와요"; icon = "❄️"; rainProb = "70%"; }
				else { description = "진눈깨비"; icon = "🌨️"; rainProb = "80%"; }
			} else { // 맑거나 흐릴 때 하늘 상태 판별
				if (sky == 1) { description = "맑음"; icon = "☀️"; rainProb = "0%"; }
				else if (sky == 3) { description = "구름많음"; icon = "⛅"; rainProb = "20%";}
				else if (sky == 4) { description = "흐림"; icon = "☁️"; rainProb = "40%"; }
			}

			result.put("temp", temp);
			result.put("humidity", humidity);
			result.put("description", description);
			result.put("icon", icon);
			result.put("rainProb", rainProb);
			result.put("dust", "좋음");

			// 5. 온도 및 강수 유무에 따른 동적 상황별 안내 멘트 조립
			String comment = generateWeatherComment(temp, pty);
			result.put("comment", comment);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("기상청 APIhub 연동 파이프라인 장애 에러 : " + e.getMessage());
			// 공공 인프라 서버 다운 대비용 복구 안전 가드 데이터 바인딩
			result.put("temp", 21);
			result.put("humidity", "50%");
			result.put("description", "맑음");
			result.put("icon", "☀️");
			result.put("rainProb", "0%");
			result.put("dust", "보통");
			result.put("comment", "축제 가기 딱 좋은 기분 좋은 날씨예요! 🎡\n행복한 하루 보내세요.");
		}

		return result;
	}

	private int[] mapRegionToGrid(String region) {
		switch (region) {
			case "서울": return new int[]{60, 127};
			case "경기": return new int[]{60, 120};
			case "인천": return new int[]{55, 124};
			case "강원": return new int[]{73, 134};
			case "대전": return new int[]{67, 100};
			case "세종": return new int[]{66, 103};
			case "충북": return new int[]{69, 107};
			case "충남": return new int[]{55, 106};
			case "광주": return new int[]{58, 74};
			case "전북": return new int[]{63, 89};
			case "전남": return new int[]{51, 67};
			case "대구": return new int[]{89, 90};
			case "부산": return new int[]{98, 76};
			case "울산": return new int[]{102, 84};
			case "경북": return new int[]{91, 106};
			case "경남": return new int[]{91, 77};
			case "제주": return new int[]{52, 38};
			default: return new int[]{60, 127};
		}
	}

	private String generateWeatherComment(int temp, int pty) {
		if (pty == 1 || pty == 4) {
			return "비가 오고 있어요! 우산을 챙기세요. 🌧️";
		}
		if (pty == 2 || pty == 3) {
			return "눈이 내려 길이 미끄러워요! 안전에 유의하세요. ❄️";
		}
		if (temp >= 28) {
			return "오늘은 햇빛이 뜨거워요!! 양산을 챙기세요. ☀️";
		}
		if (temp <= 12) {
			return "날씨가 쌀쌀해요! 외투를 준비하세요. 🍂";
		}
		return "축제 가기 딱 좋은 기분 좋은 날씨예요! 🎡";
	}

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

	private static class ForecastBaseTime {
		private String baseDate;
		private String baseTime;
	}
}