package com.bustickets.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketSaleForm {
    @NotNull
    private Long routeId;

    @NotNull
    private Long priceId;

    @NotBlank
    @Size(max = 150)
    private String passengerName;

    @NotNull
    @Min(1)
    private Integer seatNumber;

    @NotNull
    private LocalDate travelDate = LocalDate.now().plusDays(1);
}
