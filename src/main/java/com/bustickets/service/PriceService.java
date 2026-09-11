package com.bustickets.service;

import com.bustickets.model.Price;
import com.bustickets.repository.PriceRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceService {

    private final PriceRepository priceRepository;
    private final RouteService routeService;

    public PriceService(PriceRepository priceRepository, RouteService routeService) {
        this.priceRepository = priceRepository;
        this.routeService = routeService;
    }

    @Transactional(readOnly = true)
    public List<Price> findAll() {
        return priceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Price findById(Long id) {
        return priceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Цена не найдена: " + id));
    }

    @Transactional(readOnly = true)
    public List<Price> findActiveForRoute(Long routeId, LocalDate date) {
        return priceRepository.findActiveForRoute(routeId, date);
    }

    public Price save(Price price, Long routeId) {
        price.setRoute(routeService.findById(routeId));
        if (price.getValidTo() != null && price.getValidTo().isBefore(price.getValidFrom())) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
        return priceRepository.save(price);
    }

    public void delete(Long id) {
        priceRepository.deleteById(id);
    }
}
