package com.bustickets.service;

import com.bustickets.model.Price;
import com.bustickets.model.Route;
import com.bustickets.repository.PriceRepository;
import java.time.LocalDate;
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

class PriceServiceTest {
    private PriceRepository repository;
    private RouteService routeService;
    private PriceService service;

    @BeforeEach
    void setUp() {
        repository = mock(PriceRepository.class);
        routeService = mock(RouteService.class);
        service = new PriceService(repository, routeService);
    }

    @Test
    void givenValidPrice_whenSave_thenAssociatesSelectedRoute() {
        Route route = new Route();
        Price price = price(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        when(routeService.findById(3L)).thenReturn(route);
        when(repository.save(price)).thenReturn(price);

        assertSame(price, service.save(price, 3L));
        assertSame(route, price.getRoute());
        verify(repository).save(price);
    }

    @Test
    void givenEndDateBeforeStartDate_whenSave_thenRejectsPrice() {
        Price price = price(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1));
        when(routeService.findById(3L)).thenReturn(new Route());

        assertEquals("Дата окончания не может быть раньше даты начала",
                assertThrows(IllegalArgumentException.class,
                        () -> service.save(price, 3L)).getMessage());
        verify(repository, never()).save(any());
    }

    private static Price price(LocalDate from, LocalDate to) {
        Price price = new Price();
        price.setValidFrom(from);
        price.setValidTo(to);
        return price;
    }
}
