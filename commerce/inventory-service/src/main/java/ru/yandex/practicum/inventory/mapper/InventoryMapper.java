package ru.yandex.practicum.inventory.mapper;

import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static Inventory toEntity(UpdateInventoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update inventory request can't be null");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(request.productId());
        inventory.setQuantity(request.quantity());
        return inventory;
    }

    public static InventoryDto toDto(Inventory inventory, int availableQuantity) {
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory can't be null");
        }

        return new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                availableQuantity
        );
    }

}
