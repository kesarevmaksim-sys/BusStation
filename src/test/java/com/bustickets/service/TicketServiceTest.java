package com.bustickets.service;

import com.bustickets.model.Price;
import com.bustickets.model.Route;
import com.bustickets.model.Ticket;
import com.bustickets.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

class TicketServiceTest {
    private TicketRepository repository;
    private RouteService routeService;
    private PriceService priceService;
    private TicketService service;

    @BeforeEach
    void setUp() {
        repository = mock(TicketRepository.class);
        routeService = mock(RouteService.class);
        priceService = mock(PriceService.class);
        service = new TicketService(repository, routeService, priceService);
    }

    @Test
    void givenValidSale_whenSell_thenUsesSelectedRouteAndPrice() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        Route route = route(1L);
        Price price = price(2L, route);
        when(routeService.findById(1L)).thenReturn(route);
        when(priceService.findById(2L)).thenReturn(price);
        when(repository.findByRouteIdAndTravelDateAndStatus(1L, date, Ticket.Status.ACTIVE))
                .thenReturn(List.of());
        when(repository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = service.sell(1L, 2L, "Иван Иванов", 7, date);

        assertSame(route, result.getRoute());
        assertSame(price, result.getPrice());
        assertEquals("Иван Иванов", result.getPassengerName());
        assertEquals(7, result.getSeatNumber());
        assertEquals(date, result.getTravelDate());
        assertEquals(Ticket.Status.ACTIVE, result.getStatus());
    }

    @Test
    void givenPriceForAnotherRoute_whenSell_thenRejectsSale() {
        when(routeService.findById(1L)).thenReturn(route(1L));
        when(priceService.findById(2L)).thenReturn(price(2L, route(3L)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.sell(1L, 2L, "Иван Иванов", 7, LocalDate.of(2026, 10, 1)));

        assertEquals("Цена не относится к выбранному маршруту", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void givenOccupiedSeat_whenSell_thenRejectsSale() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        Route route = route(1L);
        when(routeService.findById(1L)).thenReturn(route);
        when(priceService.findById(2L)).thenReturn(price(2L, route));
        Ticket existing = new Ticket();
        existing.setSeatNumber(7);
        when(repository.findByRouteIdAndTravelDateAndStatus(1L, date, Ticket.Status.ACTIVE))
                .thenReturn(List.of(existing));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.sell(1L, 2L, "Иван Иванов", 7, date));

        assertEquals("Место 7 уже занято на эту дату", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void givenActiveTicket_whenCancel_thenMarksTicketCancelled() {
        Ticket ticket = new Ticket();
        when(repository.findById(5L)).thenReturn(Optional.of(ticket));
        when(repository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = service.cancel(5L);

        assertSame(ticket, result);
        assertEquals(Ticket.Status.CANCELLED, result.getStatus());
        verify(repository).save(ticket);
    }

    @Test
    void givenCancelledTicket_whenCancel_thenRejectsSecondCancellation() {
        Ticket ticket = new Ticket();
        ticket.setStatus(Ticket.Status.CANCELLED);
        when(repository.findById(5L)).thenReturn(Optional.of(ticket));

        assertEquals("Билет уже отменён", assertThrows(IllegalArgumentException.class,
                () -> service.cancel(5L)).getMessage());
        verify(repository, never()).save(any());
    }

    private static Route route(Long id) {
        Route route = new Route();
        route.setId(id);
        return route;
    }

    private static Price price(Long id, Route route) {
        Price price = new Price();
        price.setId(id);
        price.setRoute(route);
        return price;
    }
}
