package com.bustickets.dto;

import com.bustickets.model.City;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityForm {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String region;

    @Size(max = 100)
    private String timezoneId;

    public static CityForm from(City city) {
        CityForm form = new CityForm();
        form.setId(city.getId());
        form.setName(city.getName());
        form.setRegion(city.getRegion());
        form.setTimezoneId(city.getTimezoneId());
        return form;
    }

    public City toEntity() {
        City city = new City();
        city.setName(name);
        city.setRegion(region);
        city.setTimezoneId(timezoneId);
        return city;
    }
}
