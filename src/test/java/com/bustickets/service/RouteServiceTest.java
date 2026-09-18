package com.bustickets.service;

import com.bustickets.model.City;
import com.bustickets.model.Route;
import com.bustickets.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteServiceTest {
    private RouteRepository repository;
    private CityService cityService;
    private RouteService service;

    @BeforeEach
    void setUp() {
        repository = mock(RouteRepository.class);
        cityService = mock(CityService.class);
        service = new RouteService(repository, cityService);
    }

    @Test
    void givenDistinctCities_whenSave_thenAssociatesCities() {
        City departure = city(1L);
        City arrival = city(2L);
        Route route = new Route();
        when(cityService.findById(1L)).thenReturn(departure);
        when(cityService.findById(2L)).thenReturn(arrival);
        when(repository.save(route)).thenReturn(route);

        Route result = service.save(route, 1L, 2L);

        assertSame(route, result);
        assertSame(departure, route.getDepartureCity());
        assertSame(arrival, route.getArrivalCity());
        verify(repository).save(route);
    }

    @Test
    void givenIdenticalCities_whenSave_thenRejectsRoute() {
        assertEquals("Город отправления и прибытия должны отличаться",
                assertThrows(IllegalArgumentException.class,
                        () -> service.save(new Route(), 1L, 1L)).getMessage());
        verify(repository, never()).save(any());
    }

    private static City city(Long id) {
        City city = new City();
        city.setId(id);
        return city;
    }
}
