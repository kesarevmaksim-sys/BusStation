package com.bustickets.web;

import com.bustickets.dto.TicketSaleForm;
import com.bustickets.service.PriceService;
import com.bustickets.service.RouteService;
import com.bustickets.service.TicketService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        model.addAttribute("sale", new TicketSaleForm());
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
    public String sell(@Valid @ModelAttribute("sale") TicketSaleForm sale,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (bindingResult.hasErrors()) {
            populateSaleForm(model, sale);
            return "tickets/sell";
        }
        try {
            var ticket = ticketService.sell(sale.getRouteId(), sale.getPriceId(),
                    sale.getPassengerName(), sale.getSeatNumber(), sale.getTravelDate());
            redirectAttributes.addFlashAttribute("success",
                    "Билет №" + ticket.getId() + " продан пассажиру " + sale.getPassengerName());
            return "redirect:/tickets";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateSaleForm(model, sale);
            return "tickets/sell";
        }
    }

    private void populateSaleForm(Model model, TicketSaleForm sale) {
        model.addAttribute("pageTitle", "Продажа билета");
        model.addAttribute("routes", routeService.findAll());
        if (sale.getRouteId() != null && sale.getTravelDate() != null) {
            model.addAttribute("prices", priceService.findActiveForRoute(
                    sale.getRouteId(), sale.getTravelDate()));
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
