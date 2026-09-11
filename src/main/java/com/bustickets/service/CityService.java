package com.bustickets.service;

import com.bustickets.model.City;
import com.bustickets.repository.CityRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public List<City> findAll() {
        return cityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public City findById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город не найден: " + id));
    }

    public City save(City city) {
        if (city.getId() != null) {
            City stored = findById(city.getId());
            stored.setName(city.getName());
            stored.setRegion(city.getRegion());
            stored.setTimezoneId(city.getTimezoneId());
            return cityRepository.save(stored);
        }
        return cityRepository.save(city);
    }

    public void delete(Long id) {
        cityRepository.deleteById(id);
    }
}
