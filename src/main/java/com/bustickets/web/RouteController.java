package com.bustickets.web;

import com.bustickets.dto.RouteForm;
import com.bustickets.service.CityService;
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
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;
    private final CityService cityService;

    public RouteController(RouteService routeService, CityService cityService) {
        this.routeService = routeService;
        this.cityService = cityService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Маршруты");
        model.addAttribute("routes", routeService.findAll());
        return "routes/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateForm(model, new RouteForm());
        model.addAttribute("pageTitle", "Новый маршрут");
        return "routes/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("route") RouteForm route,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        route.setId(null);
        if (bindingResult.hasErrors()) {
            populateForm(model, route);
            model.addAttribute("pageTitle", "Новый маршрут");
            return "routes/form";
        }
        try {
            routeService.save(route.toEntity(), route.getDepartureCityId(), route.getArrivalCityId());
            redirectAttributes.addFlashAttribute("success", "Маршрут добавлен");
            return "redirect:/routes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, route);
            model.addAttribute("pageTitle", "Новый маршрут");
            return "routes/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var route = routeService.findById(id);
        populateForm(model, RouteForm.from(route));
        model.addAttribute("pageTitle", "Редактирование маршрута");
        return "routes/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("route") RouteForm route,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        route.setId(id);
        if (bindingResult.hasErrors()) {
            populateForm(model, route);
            model.addAttribute("pageTitle", "Редактирование маршрута");
            return "routes/form";
        }
        try {
            var entity = route.toEntity();
            entity.setId(id);
            routeService.save(entity, route.getDepartureCityId(), route.getArrivalCityId());
            redirectAttributes.addFlashAttribute("success", "Маршрут обновлён");
            return "redirect:/routes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateForm(model, route);
            model.addAttribute("pageTitle", "Редактирование маршрута");
            return "routes/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            routeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Маршрут удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить маршрут");
        }
        return "redirect:/routes";
    }

    private void populateForm(Model model, RouteForm route) {
        model.addAttribute("route", route);
        model.addAttribute("cities", cityService.findAll());
    }
}
