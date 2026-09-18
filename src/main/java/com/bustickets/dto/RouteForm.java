package com.bustickets.dto;

import com.bustickets.model.Route;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteForm {
    private Long id;

    @NotNull
    private Long departureCityId;

    @NotNull
    private Long arrivalCityId;

    @NotNull
    private LocalTime departureTime;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    public static RouteForm from(Route route) {
        RouteForm form = new RouteForm();
        form.setId(route.getId());
        form.setDepartureCityId(route.getDepartureCity().getId());
        form.setArrivalCityId(route.getArrivalCity().getId());
        form.setDepartureTime(route.getDepartureTime());
        form.setDurationMinutes(route.getDurationMinutes());
        return form;
    }

    public Route toEntity() {
        Route route = new Route();
        route.setDepartureTime(departureTime);
        route.setDurationMinutes(durationMinutes);
        return route;
    }
}
