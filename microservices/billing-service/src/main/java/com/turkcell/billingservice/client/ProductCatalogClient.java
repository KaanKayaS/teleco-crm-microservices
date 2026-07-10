package com.turkcell.billingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

@FeignClient(name = "product-catalog-service", url = "${product.catalog.url:http://localhost:8083}")
public interface ProductCatalogClient {

    @GetMapping("/api/v1/tariffs/{code}")
    Optional<TariffDto> getTariffByCode(@PathVariable("code") String code);
}
