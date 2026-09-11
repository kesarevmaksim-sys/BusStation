package com.bustickets.repository;

import com.bustickets.model.Price;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByRouteIdOrderByValidFromDesc(Long routeId);

    @Query("""
            SELECT p FROM Price p
            WHERE p.route.id = :routeId
              AND p.validFrom <= :date
              AND (p.validTo IS NULL OR p.validTo >= :date)
            ORDER BY p.validFrom DESC
            """)
    List<Price> findActiveForRoute(@Param("routeId") Long routeId, @Param("date") java.time.LocalDate date);
}
