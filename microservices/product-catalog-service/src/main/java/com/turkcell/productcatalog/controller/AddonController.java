package com.turkcell.productcatalog.controller;

import com.turkcell.productcatalog.model.entity.Addon;
import com.turkcell.productcatalog.service.AddonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addons")
public class AddonController {

    private final AddonService addonService;

    public AddonController(AddonService addonService) {
        this.addonService = addonService;
    }

    @GetMapping
    public ResponseEntity<List<Addon>> getAddons(@RequestParam(required = false) String tariffCode) {
        return ResponseEntity.ok(addonService.getAddons(tariffCode));
    }

    @PostMapping
    public ResponseEntity<Addon> createAddon(@RequestBody Addon addon) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addonService.createAddon(addon));
    }

    @PostMapping("/{addonCode}/assign-to-tariff/{tariffCode}")
    public ResponseEntity<Void> assignAddonToTariff(@PathVariable String addonCode, @PathVariable String tariffCode) {
        addonService.assignAddonToTariff(addonCode, tariffCode);
        return ResponseEntity.ok().build();
    }
}
