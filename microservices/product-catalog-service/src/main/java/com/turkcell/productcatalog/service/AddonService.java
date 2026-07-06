package com.turkcell.productcatalog.service;

import com.turkcell.productcatalog.model.entity.Addon;
import com.turkcell.productcatalog.model.entity.Tariff;
import com.turkcell.productcatalog.repository.AddonRepository;
import com.turkcell.productcatalog.repository.TariffRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddonService {

    private final AddonRepository addonRepository;
    private final TariffRepository tariffRepository;

    public AddonService(AddonRepository addonRepository, TariffRepository tariffRepository) {
        this.addonRepository = addonRepository;
        this.tariffRepository = tariffRepository;
    }

    @Cacheable(value = "addons", key = "#tariffCode != null ? #tariffCode : 'all'")
    public List<Addon> getAddons(String tariffCode) {
        if (tariffCode != null && !tariffCode.isBlank()) {
            Optional<Tariff> tariffOpt = tariffRepository.findByCode(tariffCode);
            if (tariffOpt.isPresent()) {
                return tariffOpt.get().getAddons().stream().collect(Collectors.toList());
            }
            return List.of();
        }
        return addonRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "addons", allEntries = true)
    public Addon createAddon(Addon addon) {
        addon.setId(null);
        return addonRepository.save(addon);
    }

    @Transactional
    @CacheEvict(value = {"addons", "tariffs", "tariff"}, allEntries = true)
    public void assignAddonToTariff(String addonCode, String tariffCode) {
        Addon addon = addonRepository.findByCode(addonCode)
                .orElseThrow(() -> new RuntimeException("Addon not found"));
        Tariff tariff = tariffRepository.findByCode(tariffCode)
                .orElseThrow(() -> new RuntimeException("Tariff not found"));

        tariff.addAddon(addon);
        tariffRepository.save(tariff);
    }
}
