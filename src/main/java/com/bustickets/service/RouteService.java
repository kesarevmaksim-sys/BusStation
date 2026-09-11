package com.bustickets.service;

import com.bustickets.model.Route;
import com.bustickets.repository.RouteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;
    private final CityService cityService;

    public RouteService(RouteRepository routeRepository, CityService cityService) {
        this.routeRepository = routeRepository;
        this.cityService = cityService;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден: " + id));
    }

    public Route save(Route route, Long departureCityId, Long arrivalCityId) {
        if (departureCityId.equals(arrivalCityId)) {
            throw new IllegalArgumentException("Город отправления и прибытия должны отличаться");
        }
        route.setDepartureCity(cityService.findById(departureCityId));
        route.setArrivalCity(cityService.findById(arrivalCityId));
        return routeRepository.save(route);
    }

    public void delete(Long id) {
        routeRepository.deleteById(id);
    }
}
