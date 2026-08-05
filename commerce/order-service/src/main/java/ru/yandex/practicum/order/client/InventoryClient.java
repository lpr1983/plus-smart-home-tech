package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.dto.InventoryReleaseRequestDto;
import ru.yandex.practicum.order.dto.InventoryReserveRequestDto;
import ru.yandex.practicum.order.dto.InventoryReserveResponseDto;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve")
    InventoryReserveResponseDto reserveStock(@RequestBody InventoryReserveRequestDto request);

    @PostMapping("/api/inventory/release")
    InventoryReserveResponseDto releaseStock(@RequestBody InventoryReleaseRequestDto request);
}
