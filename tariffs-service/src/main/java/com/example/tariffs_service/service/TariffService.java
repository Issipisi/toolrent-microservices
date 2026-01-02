package com.example.tariffs_service.service;

import com.example.tariffs_service.entity.TariffEntity;
import com.example.tariffs_service.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffEntity createTariff(Double dailyRentalRate, Double dailyFineRate) {
        TariffEntity tariff = new TariffEntity();
        tariff.setDailyRentalRate(dailyRentalRate);
        tariff.setDailyFineRate(dailyFineRate);
        return tariffRepository.save(tariff);
    }

    public TariffEntity updateTariff(Long id, Double dailyRentalRate, Double dailyFineRate) {
        TariffEntity tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));

        tariff.setDailyRentalRate(dailyRentalRate);
        tariff.setDailyFineRate(dailyFineRate);

        return tariffRepository.save(tariff);
    }

    public TariffEntity getTariff(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));
    }

    public List<TariffEntity> getAllTariffs() {
        return tariffRepository.findAll();
    }

    // Método especial para Tool Service: crear o actualizar
    public TariffEntity createOrUpdateTariff(Long id, Double dailyRentalRate, Double dailyFineRate) {
        if (id != null && tariffRepository.existsById(id)) {
            return updateTariff(id, dailyRentalRate, dailyFineRate);
        } else {
            return createTariff(dailyRentalRate, dailyFineRate);
        }
    }
}