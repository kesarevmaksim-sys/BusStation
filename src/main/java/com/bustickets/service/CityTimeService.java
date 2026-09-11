package com.bustickets.service;

import com.bustickets.model.City;
import com.bustickets.repository.CityRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CityTimeService {

    private static final Logger log = LoggerFactory.getLogger(CityTimeService.class);

    private final CityRepository cityRepository;
    private final RestClient restClient;

    public CityTimeService(CityRepository cityRepository,
                           RestClient.Builder restClientBuilder,
                           @Value("${time-api.base-url}") String baseUrl) {
        this.cityRepository = cityRepository;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Scheduled(fixedDelayString = "${time-api.update-delay-ms:300000}", initialDelayString = "${time-api.initial-delay-ms:5000}")
    public void updateAllCities() {
        List<City> cities = cityRepository.findAll().stream()
                .filter(city -> city.getTimezoneId() != null && !city.getTimezoneId().isBlank())
                .toList();
        Map<String, TimeApiResponse> responsesByTimezone = new HashMap<>();

        for (City city : cities) {
            try {
                TimeApiResponse response = responsesByTimezone.computeIfAbsent(
                        city.getTimezoneId(), this::requestTime);
                if (response != null) {
                    saveTime(city.getId(), response);
                }
            } catch (Exception exception) {
                log.warn("Не удалось обновить время для города {} ({}): {}",
                        city.getName(), city.getTimezoneId(), exception.getMessage());
            }
        }
    }

    private TimeApiResponse requestTime(String timezoneId) {
        return restClient.get()
                .uri(builder -> builder.path("/api/time/current/zone")
                        .queryParam("timeZone", timezoneId)
                        .build())
                .retrieve()
                .body(TimeApiResponse.class);
    }

    public void saveTime(Long cityId, TimeApiResponse response) {
        cityRepository.findById(cityId).ifPresent(city -> {
            ZoneId zoneId = ZoneId.of(response.timeZone());
            ZonedDateTime cityTime = response.dateTime().atZone(zoneId);
            city.setTimezoneId(response.timeZone());
            city.setTimezoneAbbreviation(cityTime.format(DateTimeFormatter.ofPattern("z", Locale.ENGLISH)));
            city.setUtcOffset(cityTime.getOffset().getId());
            city.setCurrentTime(cityTime.toOffsetDateTime());
            city.setTimeUpdatedAt(OffsetDateTime.now());
            cityRepository.save(city);
        });
    }

    public record TimeApiResponse(
            LocalDateTime dateTime,
            String timeZone
    ) {
    }
}
