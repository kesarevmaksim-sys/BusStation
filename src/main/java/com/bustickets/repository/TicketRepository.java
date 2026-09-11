package com.bustickets.repository;

import com.bustickets.model.Ticket;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderBySoldAtDesc();

    List<Ticket> findByRouteIdAndTravelDateAndStatus(
            Long routeId, LocalDate travelDate, Ticket.Status status);
}
