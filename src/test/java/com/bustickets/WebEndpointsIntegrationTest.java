package com.bustickets;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:web_endpoints_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "time-api.initial-delay-ms=3600000"
})
class WebEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seedData() {
        jdbc.update("DELETE FROM tickets");
        jdbc.update("DELETE FROM prices");
        jdbc.update("DELETE FROM routes");
        jdbc.update("DELETE FROM cities");
        jdbc.update("INSERT INTO cities (id, name) VALUES (1001, 'Москва'), (1002, 'Казань')");
        jdbc.update("INSERT INTO routes (id, departure_city_id, arrival_city_id, departure_time, duration_minutes) "
                + "VALUES (1001, 1001, 1002, '08:00', 120)");
        jdbc.update("INSERT INTO prices (id, route_id, amount, valid_from) "
                + "VALUES (1001, 1001, 100.00, '2020-01-01')");
        jdbc.update("INSERT INTO tickets (id, route_id, price_id, passenger_name, seat_number, travel_date, status, sold_at) "
                + "VALUES (1001, 1001, 1001, 'Пассажир', 1, ?, 'ACTIVE', CURRENT_TIMESTAMP)",
                LocalDate.now().plusDays(1));
    }

    static Stream<Arguments> pages() {
        return Stream.of(
                Arguments.of("/", "index"),
                Arguments.of("/cities", "cities/list"),
                Arguments.of("/cities/new", "cities/form"),
                Arguments.of("/cities/1001/edit", "cities/form"),
                Arguments.of("/routes", "routes/list"),
                Arguments.of("/routes/new", "routes/form"),
                Arguments.of("/routes/1001/edit", "routes/form"),
                Arguments.of("/prices", "prices/list"),
                Arguments.of("/prices/new", "prices/form"),
                Arguments.of("/prices/1001/edit", "prices/form"),
                Arguments.of("/tickets", "tickets/list"),
                Arguments.of("/tickets/sell", "tickets/sell"));
    }

    @ParameterizedTest
    @MethodSource("pages")
    void givenSeedData_whenPageIsRequested_thenReturnsExpectedView(String path, String expectedView) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name(expectedView));
    }

    @Test
    void givenActivePrice_whenPricesFragmentIsRequested_thenReturnsSuccess() throws Exception {
        mockMvc.perform(get("/tickets/sell/prices")
                        .param("routeId", "1001")
                        .param("travelDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("tickets/prices-fragment :: prices"));
    }

    @Test
    void givenInvalidRouteId_whenPricesFragmentIsRequested_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/tickets/sell/prices").param("routeId", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenSeedData_whenCitiesAreCreatedUpdatedAndDeleted_thenRedirectsAndPersistsChanges() throws Exception {
        mockMvc.perform(post("/cities").param("name", "Самара"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cities"));
        assertThat(count("cities", "name = 'Самара'")).isEqualTo(1);

        mockMvc.perform(post("/cities/1001").param("name", "Москва новая"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cities"));
        assertThat(count("cities", "id = 1001 AND name = 'Москва новая'")).isEqualTo(1);

        Long newCityId = jdbc.queryForObject("SELECT id FROM cities WHERE name = 'Самара'", Long.class);
        mockMvc.perform(post("/cities/{id}/delete", newCityId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "Город удалён"));
        assertThat(count("cities", "id = " + newCityId)).isZero();

        mockMvc.perform(post("/cities/1002/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cities"))
                .andExpect(flash().attribute("error", "Не удалось удалить город: используется в маршрутах"));
    }

    @Test
    void givenBlankCityName_whenCityIsSubmitted_thenReturnsFormWithValidationErrors() throws Exception {
        mockMvc.perform(post("/cities").param("name", " "))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("cities/form"))
                .andExpect(model().attributeHasFieldErrors("city", "name"));
        mockMvc.perform(post("/cities/1001").param("name", " "))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("cities/form"))
                .andExpect(model().attributeHasFieldErrors("city", "name"));
    }

    @Test
    void givenSeedData_whenRoutesAreCreatedUpdatedAndDeleted_thenRedirectsAndPersistsChanges() throws Exception {
        mockMvc.perform(post("/routes")
                        .param("departureCityId", "1002").param("arrivalCityId", "1001")
                        .param("departureTime", "09:00").param("durationMinutes", "180"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"));
        assertThat(count("routes", "departure_city_id = 1002")).isEqualTo(1);

        mockMvc.perform(post("/routes/1001")
                        .param("departureCityId", "1001").param("arrivalCityId", "1002")
                        .param("departureTime", "10:00").param("durationMinutes", "120"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"));
        assertThat(count("routes", "id = 1001 AND departure_time = '10:00:00'")).isEqualTo(1);

        Long newRouteId = jdbc.queryForObject(
                "SELECT id FROM routes WHERE departure_city_id = 1002", Long.class);
        mockMvc.perform(post("/routes/{id}/delete", newRouteId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "Маршрут удалён"));
        assertThat(count("routes", "id = " + newRouteId)).isZero();

        mockMvc.perform(post("/routes/1001/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"))
                .andExpect(flash().attribute("error", "Не удалось удалить маршрут"));
    }

    @Test
    void givenInvalidRoute_whenRouteIsSubmitted_thenReturnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/routes").param("departureCityId", "1001"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("routes/form"))
                .andExpect(model().attributeHasFieldErrors("route", "arrivalCityId"));

        mockMvc.perform(post("/routes/1001")
                        .param("departureCityId", "1001").param("arrivalCityId", "1001")
                        .param("departureTime", "08:00").param("durationMinutes", "120"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("routes/form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void givenSeedData_whenPricesAreCreatedUpdatedAndDeleted_thenRedirectsAndPersistsChanges() throws Exception {
        mockMvc.perform(post("/prices").param("routeId", "1001")
                        .param("amount", "200.00").param("validFrom", "2020-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prices"));
        assertThat(count("prices", "amount = 200.00")).isEqualTo(1);

        mockMvc.perform(post("/prices/1001").param("routeId", "1001")
                        .param("amount", "150.00").param("validFrom", "2020-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prices"));
        assertThat(count("prices", "id = 1001 AND amount = 150.00")).isEqualTo(1);

        Long newPriceId = jdbc.queryForObject(
                "SELECT id FROM prices WHERE amount = 200.00", Long.class);
        mockMvc.perform(post("/prices/{id}/delete", newPriceId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prices"));
        assertThat(count("prices", "id = " + newPriceId)).isZero();
    }

    @Test
    void givenInvalidPrice_whenPriceIsSubmitted_thenReturnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/prices").param("routeId", "1001")
                        .param("amount", "0").param("validFrom", "2020-01-01"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("prices/form"))
                .andExpect(model().attributeHasFieldErrors("price", "amount"));

        mockMvc.perform(post("/prices/1001").param("routeId", "1001")
                        .param("amount", "100.00").param("validFrom", "2020-02-01")
                        .param("validTo", "2020-01-01"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("prices/form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void givenSeedData_whenTicketIsSoldAndCancelled_thenRedirectsAndPersistsChanges() throws Exception {
        mockMvc.perform(post("/tickets/sell").param("routeId", "1001")
                        .param("priceId", "1001").param("passengerName", "Новый пассажир")
                        .param("seatNumber", "2")
                        .param("travelDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));
        assertThat(count("tickets", "seat_number = 2")).isEqualTo(1);

        mockMvc.perform(post("/tickets/1001/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"))
                .andExpect(flash().attribute("success", "Билет отменён"));
        assertThat(count("tickets", "id = 1001 AND status = 'CANCELLED'")).isEqualTo(1);
    }

    @Test
    void givenInvalidSaleOrCancelledTicket_whenTicketIsSubmitted_thenReturnsErrors() throws Exception {
        mockMvc.perform(post("/tickets/sell").param("routeId", "1001")
                        .param("priceId", "1001").param("passengerName", " ")
                        .param("seatNumber", "2"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("tickets/sell"))
                .andExpect(model().attributeHasFieldErrors("sale", "passengerName"));

        mockMvc.perform(post("/tickets/sell").param("routeId", "1001")
                        .param("priceId", "1001").param("passengerName", "Другой пассажир")
                        .param("seatNumber", "1")
                        .param("travelDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("tickets/sell"))
                .andExpect(model().attributeExists("error"));

        mockMvc.perform(post("/tickets/1001/cancel"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/tickets/1001/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Билет уже отменён"));
    }

    private int count(String table, String condition) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + condition, Integer.class);
    }
}
