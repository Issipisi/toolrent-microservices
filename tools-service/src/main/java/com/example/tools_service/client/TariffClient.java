package com.example.tools_service.client;

import com.example.tools_service.model.TariffModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "tariffs-service", path = "/api/tariffs")
public interface TariffClient {

    @PostMapping
    TariffModel createTariff(
            @RequestParam Double dailyRentalRate,
            @RequestParam Double dailyFineRate);

    @PutMapping("/{id}")
    TariffModel updateTariff(
            @PathVariable Long id,
            @RequestParam Double dailyRentalRate,
            @RequestParam Double dailyFineRate);

    @GetMapping("/{id}")
    TariffModel getTariff(@PathVariable Long id);
}