package com.bustickets.web;

import com.bustickets.model.Price;
import com.bustickets.service.PriceService;
import com.bustickets.service.RouteService;
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
@RequestMapping("/prices")
public class PriceController {

    private final PriceService priceService;
    private final RouteService routeService;

    public PriceController(PriceService priceService, RouteService routeService) {
        this.priceService = priceService;
        this.routeService = routeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Цены");
        model.addAttribute("prices", priceService.findAll());
        return "prices/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateForm(model, newPrice(), null);
        model.addAttribute("pageTitle", "Новая цена");
        return "prices/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("price") Price price,
                         BindingResult bindingResult,
                         @RequestParam Long routeId,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            populateForm(model, price, routeId);
            model.addAttribute("pageTitle", "Новая цена");
            return "prices/form";
        }
        try {
            priceService.save(price, routeId);
            redirectAttributes.addFlashAttribute("success", "Цена добавлена");
            return "redirect:/prices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, price, routeId);
            model.addAttribute("pageTitle", "Новая цена");
            return "prices/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var price = priceService.findById(id);
        populateForm(model, price, price.getRoute().getId());
        model.addAttribute("pageTitle", "Редактирование цены");
        return "prices/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("price") Price price,
                         BindingResult bindingResult,
                         @RequestParam Long routeId,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            populateForm(model, price, routeId);
            model.addAttribute("pageTitle", "Редактирование цены");
            return "prices/form";
        }
        try {
            price.setId(id);
            priceService.save(price, routeId);
            redirectAttributes.addFlashAttribute("success", "Цена обновлена");
            return "redirect:/prices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, price, routeId);
            model.addAttribute("pageTitle", "Редактирование цены");
            return "prices/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        priceService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Цена удалена");
        return "redirect:/prices";
    }

    private Price newPrice() {
        var price = new Price();
        price.setValidFrom(LocalDate.now());
        return price;
    }

    private void populateForm(Model model, Price price, Long routeId) {
        model.addAttribute("price", price);
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("routeId", routeId);
    }
}
