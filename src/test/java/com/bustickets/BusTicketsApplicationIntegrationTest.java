package com.bustickets;

import com.bustickets.repository.CityRepository;
import com.bustickets.service.CityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(
        useMainMethod = ALWAYS,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "time-api.initial-delay-ms=3600000"
        })
class BusTicketsApplicationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CityService cityService;

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private CityRepository cityRepository;

    @Test
    void givenPostgresContainer_whenMainStartsAndCitiesAreRead_thenRepositoryIsCalledOnce() {
        assertThat(applicationContext.getBean(BusTicketsApplication.class)).isNotNull();
        assertThat(cityService.findAll()).hasSize(5);
        verify(cityRepository, times(1)).findAll();
    }

    @Test
    void givenRunningApplication_whenCitiesPageIsRequested_thenReturnsSuccessfulStatus() throws Exception {
        mockMvc.perform(get("/cities"))
                .andExpect(status().is2xxSuccessful());
        verify(cityRepository, times(1)).findAll();
    }
}
