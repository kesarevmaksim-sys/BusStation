package com.bustickets.service;

import com.bustickets.model.City;
import com.bustickets.repository.CityRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CityServiceTest {
    private CityRepository repository;
    private CityService service;

    @BeforeEach
    void setUp() {
        repository = mock(CityRepository.class);
        service = new CityService(repository);
    }

    @Test
    void givenStoredCity_whenSave_thenUpdatesEditableFields() {
        City stored = new City();
        stored.setId(4L);
        stored.setTimezoneAbbreviation("MSK");
        City input = new City();
        input.setId(4L);
        input.setName("Москва");
        input.setRegion("Московская область");
        input.setTimezoneId("Europe/Moscow");
        when(repository.findById(4L)).thenReturn(Optional.of(stored));
        when(repository.save(stored)).thenReturn(stored);

        City result = service.save(input);

        assertSame(stored, result);
        assertEquals("Москва", stored.getName());
        assertEquals("Московская область", stored.getRegion());
        assertEquals("Europe/Moscow", stored.getTimezoneId());
        assertEquals("MSK", stored.getTimezoneAbbreviation());
        verify(repository).save(stored);
    }

    @Test
    void givenMissingCity_whenFindById_thenReportsError() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertEquals("Город не найден: 4", assertThrows(IllegalArgumentException.class,
                () -> service.findById(4L)).getMessage());
    }
}
