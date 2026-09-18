package com.bustickets.dto;

import com.bustickets.model.Price;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceForm {
    private Long id;

    @NotNull
    private Long routeId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate validFrom = LocalDate.now();

    private LocalDate validTo;

    public static PriceForm from(Price price) {
        PriceForm form = new PriceForm();
        form.setId(price.getId());
        form.setRouteId(price.getRoute().getId());
        form.setAmount(price.getAmount());
        form.setValidFrom(price.getValidFrom());
        form.setValidTo(price.getValidTo());
        return form;
    }

    public Price toEntity() {
        Price price = new Price();
        price.setAmount(amount);
        price.setValidFrom(validFrom);
        price.setValidTo(validTo);
        return price;
    }
}
