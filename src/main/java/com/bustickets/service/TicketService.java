package com.bustickets.service;

import com.bustickets.model.Price;
import com.bustickets.model.Ticket;
import com.bustickets.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final RouteService routeService;
    private final PriceService priceService;

    public TicketService(
            TicketRepository ticketRepository,
            RouteService routeService,
            PriceService priceService) {
        this.ticketRepository = ticketRepository;
        this.routeService = routeService;
        this.priceService = priceService;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAllByOrderBySoldAtDesc();
    }

    @Transactional(readOnly = true)
    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден: " + id));
    }

    public Ticket sell(Long routeId, Long priceId, String passengerName, Integer seatNumber, LocalDate travelDate) {
        var route = routeService.findById(routeId);
        var price = priceService.findById(priceId);

        if (!price.getRoute().getId().equals(routeId)) {
            throw new IllegalArgumentException("Цена не относится к выбранному маршруту");
        }

        boolean seatTaken = ticketRepository
                .findByRouteIdAndTravelDateAndStatus(routeId, travelDate, Ticket.Status.ACTIVE)
                .stream()
                .anyMatch(t -> t.getSeatNumber().equals(seatNumber));
        if (seatTaken) {
            throw new IllegalArgumentException("Место " + seatNumber + " уже занято на эту дату");
        }

        var ticket = new Ticket();
        ticket.setRoute(route);
        ticket.setPrice(price);
        ticket.setPassengerName(passengerName);
        ticket.setSeatNumber(seatNumber);
        ticket.setTravelDate(travelDate);
        ticket.setStatus(Ticket.Status.ACTIVE);
        return ticketRepository.save(ticket);
    }

    public Ticket cancel(Long id) {
        var ticket = findById(id);
        if (ticket.getStatus() == Ticket.Status.CANCELLED) {
            throw new IllegalArgumentException("Билет уже отменён");
        }
        ticket.setStatus(Ticket.Status.CANCELLED);
        return ticketRepository.save(ticket);
    }
}
