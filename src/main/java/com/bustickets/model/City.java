package com.bustickets.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "cities")
@Getter
@Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String region;

    @Size(max = 100)
    @Column(name = "timezone_id", length = 100)
    private String timezoneId;

    @Size(max = 10)
    @Column(name = "timezone_abbreviation", length = 10)
    private String timezoneAbbreviation;

    @Size(max = 6)
    @Column(name = "utc_offset", length = 6)
    private String utcOffset;

    @Column(name = "city_current_time")
    private OffsetDateTime currentTime;

    @Column(name = "time_updated_at")
    private OffsetDateTime timeUpdatedAt;

    @Transient
    public ZonedDateTime getLocalCurrentTime() {
        if (currentTime == null || timezoneId == null || timezoneId.isBlank()) {
            return null;
        }
        return currentTime.atZoneSameInstant(ZoneId.of(timezoneId));
    }

    @Transient
    public ZonedDateTime getLocalTimeUpdatedAt() {
        return timeUpdatedAt == null
                ? null
                : timeUpdatedAt.atZoneSameInstant(ZoneId.systemDefault());
    }
}
