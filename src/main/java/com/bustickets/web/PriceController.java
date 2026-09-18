package com.bustickets.web;

import com.bustickets.dto.PriceForm;
import com.bustickets.service.PriceService;
import com.bustickets.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        populateForm(model, new PriceForm());
        model.addAttribute("pageTitle", "Новая цена");
        return "prices/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("price") PriceForm price,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        price.setId(null);
        if (bindingResult.hasErrors()) {
            populateForm(model, price);
            model.addAttribute("pageTitle", "Новая цена");
            return "prices/form";
        }
        try {
            priceService.save(price.toEntity(), price.getRouteId());
            redirectAttributes.addFlashAttribute("success", "Цена добавлена");
            return "redirect:/prices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, price);
            model.addAttribute("pageTitle", "Новая цена");
            return "prices/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var price = priceService.findById(id);
        populateForm(model, PriceForm.from(price));
        model.addAttribute("pageTitle", "Редактирование цены");
        return "prices/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("price") PriceForm price,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        price.setId(id);
        if (bindingResult.hasErrors()) {
            populateForm(model, price);
            model.addAttribute("pageTitle", "Редактирование цены");
            return "prices/form";
        }
        try {
            var entity = price.toEntity();
            entity.setId(id);
            priceService.save(entity, price.getRouteId());
            redirectAttributes.addFlashAttribute("success", "Цена обновлена");
            return "redirect:/prices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, price);
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

    private void populateForm(Model model, PriceForm price) {
        model.addAttribute("price", price);
        model.addAttribute("routes", routeService.findAll());
    }
}
