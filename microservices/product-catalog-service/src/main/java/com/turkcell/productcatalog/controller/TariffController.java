package com.turkcell.productcatalog.controller;

import com.turkcell.productcatalog.model.entity.Tariff;
import com.turkcell.productcatalog.service.TariffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tariffs")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping
    public ResponseEntity<List<Tariff>> getAllTariffs() {
        return ResponseEntity.ok(tariffService.getAllTariffs());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Tariff> getTariffByCode(@PathVariable String code) {
        return tariffService.getTariffByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tariff> createTariff(@RequestBody Tariff tariff) {
        Tariff createdTariff = tariffService.createTariff(tariff);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTariff);
    }

    @PatchMapping("/{code}/price")
    public ResponseEntity<Tariff> updateTariffPrice(@PathVariable String code, @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal newPrice = payload.get("price");
        if (newPrice == null) {
            return ResponseEntity.badRequest().build();
        }
        Tariff updatedTariff = tariffService.updateTariffPrice(code, newPrice);
        return ResponseEntity.ok(updatedTariff);
    }
}
