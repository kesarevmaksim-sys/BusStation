package com.bustickets.web;

import com.bustickets.model.City;
import com.bustickets.service.CityService;
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
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Города");
        model.addAttribute("cities", cityService.findAll());
        return "cities/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Новый город");
        model.addAttribute("city", new City());
        return "cities/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("city") City city,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "cities/form";
        }
        cityService.save(city);
        redirectAttributes.addFlashAttribute("success", "Город добавлен");
        return "redirect:/cities";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Редактирование города");
        model.addAttribute("city", cityService.findById(id));
        return "cities/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("city") City city,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "cities/form";
        }
        city.setId(id);
        cityService.save(city);
        redirectAttributes.addFlashAttribute("success", "Город обновлён");
        return "redirect:/cities";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cityService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Город удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить город: используется в маршрутах");
        }
        return "redirect:/cities";
    }
}
