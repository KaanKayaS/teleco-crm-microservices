package com.turkcell.productcatalog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.productcatalog.model.entity.OutboxEvent;
import com.turkcell.productcatalog.model.entity.Tariff;
import com.turkcell.productcatalog.repository.OutboxEventRepository;
import com.turkcell.productcatalog.repository.TariffRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TariffService {

    private final TariffRepository tariffRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TariffService(TariffRepository tariffRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.tariffRepository = tariffRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = "tariffs")
    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }

    @Cacheable(value = "tariff", key = "#code")
    public Optional<Tariff> getTariffByCode(String code) {
        return tariffRepository.findByCode(code);
    }

    @Transactional
    @CacheEvict(value = {"tariffs", "tariff"}, allEntries = true)
    public Tariff createTariff(Tariff tariff) {
        tariff.setId(null);
        Tariff savedTariff = tariffRepository.save(tariff);
        
        saveOutboxEvent(savedTariff, "TariffCreated");
        
        return savedTariff;
    }

    @Transactional
    @CacheEvict(value = {"tariffs", "tariff"}, allEntries = true)
    public Tariff updateTariffPrice(String code, java.math.BigDecimal newPrice) {
        Tariff tariff = tariffRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Tariff not found"));
            
        tariff.setMonthlyFee(newPrice);
        Tariff updatedTariff = tariffRepository.save(tariff);
        
        saveOutboxEvent(updatedTariff, "TariffPriceChanged");
        
        return updatedTariff;
    }

    private void saveOutboxEvent(Tariff tariff, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Tariff");
        event.setAggregateId(tariff.getId().toString());
        event.setType(eventType);
        event.setCreatedAt(ZonedDateTime.now());
        event.setStatus("PENDING");
        
        try {
            String payload = objectMapper.writeValueAsString(tariff);
            event.setPayload(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
        
        outboxEventRepository.save(event);
    }
}
