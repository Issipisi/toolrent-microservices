package com.example.tariffs_service.controller;

import com.example.tariffs_service.entity.TariffEntity;
import com.example.tariffs_service.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    public ResponseEntity<TariffEntity> createTariff(
            @RequestParam Double dailyRentalRate,
            @RequestParam Double dailyFineRate) {
        TariffEntity tariff = tariffService.createTariff(dailyRentalRate, dailyFineRate);
        return ResponseEntity.ok(tariff);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TariffEntity> updateTariff(
            @PathVariable Long id,
            @RequestParam Double dailyRentalRate,
            @RequestParam Double dailyFineRate) {
        TariffEntity tariff = tariffService.updateTariff(id, dailyRentalRate, dailyFineRate);
        return ResponseEntity.ok(tariff);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TariffEntity> getTariff(@PathVariable Long id) {
        TariffEntity tariff = tariffService.getTariff(id);
        return ResponseEntity.ok(tariff);
    }

    @GetMapping
    public ResponseEntity<List<TariffEntity>> getAllTariffs() {
        List<TariffEntity> tariffs = tariffService.getAllTariffs();
        return ResponseEntity.ok(tariffs);
    }
}