package com.bustickets.web;

import com.bustickets.service.PriceService;
import com.bustickets.service.RouteService;
import com.bustickets.service.TicketService;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final RouteService routeService;
    private final PriceService priceService;

    public TicketController(
            TicketService ticketService,
            RouteService routeService,
            PriceService priceService) {
        this.ticketService = ticketService;
        this.routeService = routeService;
        this.priceService = priceService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Билеты");
        model.addAttribute("tickets", ticketService.findAll());
        return "tickets/list";
    }

    @GetMapping("/sell")
    public String sellForm(Model model) {
        model.addAttribute("pageTitle", "Продажа билета");
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("travelDate", LocalDate.now().plusDays(1));
        return "tickets/sell";
    }

    @GetMapping("/sell/prices")
    public String pricesForRoute(@RequestParam Long routeId,
                                 @RequestParam LocalDate travelDate,
                                 Model model) {
        model.addAttribute("prices", priceService.findActiveForRoute(routeId, travelDate));
        return "tickets/prices-fragment :: prices";
    }

    @PostMapping("/sell")
    public String sell(@RequestParam Long routeId,
                       @RequestParam Long priceId,
                       @RequestParam String passengerName,
                       @RequestParam Integer seatNumber,
                       @RequestParam LocalDate travelDate,
                       RedirectAttributes redirectAttributes) {
        try {
            var ticket = ticketService.sell(routeId, priceId, passengerName, seatNumber, travelDate);
            redirectAttributes.addFlashAttribute("success",
                    "Билет №" + ticket.getId() + " продан пассажиру " + passengerName);
            return "redirect:/tickets";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tickets/sell";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Билет отменён");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets";
    }
}
