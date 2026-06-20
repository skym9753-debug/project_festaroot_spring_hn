package com.study.app.domains.weather;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

	@Autowired
	@Qualifier("liveWeatherService")
	private WeatherService weatherService;

	@GetMapping("")
	public ResponseEntity<Map<String, Object>> getWeatherInfo(@RequestParam("region") String region) {
		Map<String, Object> weatherData = weatherService.getLiveWeather(region);
		return ResponseEntity.ok(weatherData);
	}
}