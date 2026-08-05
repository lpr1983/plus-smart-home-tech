package ru.yandex.practicum.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.ConflictException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryDto createInventory(UpdateInventoryRequest request) {
        log.info("Creating inventory: productId={}, quantity={}", request.productId(), request.quantity());

        if (inventoryRepository.existsByProductId(request.productId())) {
            log.warn("Inventory already exists: productId={}", request.productId());
            throw new ConflictException(String.format(
                    "Inventory for product %d already exists",
                    request.productId()
            ));
        }

        Inventory inventory = inventoryRepository.saveAndFlush(InventoryMapper.toEntity(request));
        log.info("Inventory created: id={}, productId={}", inventory.getId(), inventory.getProductId());
        return toDto(inventory);
    }

    @Transactional
    public InventoryDto updateInventory(UpdateInventoryRequest request) {
        log.info("Updating inventory: productId={}, quantity={}", request.productId(), request.quantity());

        Inventory inventory = findByProductId(request.productId());
        inventory.setQuantity(request.quantity());
        Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);

        log.info("Inventory updated: productId={}", savedInventory.getProductId());
        return toDto(savedInventory);
    }

    public List<InventoryDto> getAllInventory() {
        log.debug("Getting all inventory records");

        List<InventoryDto> inventory = inventoryRepository.findAll().stream()
                .map(InventoryService::toDto)
                .toList();

        log.debug("Inventory records retrieved: count={}", inventory.size());
        return inventory;
    }

    public InventoryDto getByProductId(Long productId) {
        log.debug("Getting inventory: productId={}", productId);
        return toDto(findByProductId(productId));
    }

    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        log.info("Reserving stock: productId={}, quantity={}", request.productId(), request.quantity());

        Inventory inventory = findByProductId(request.productId());
        int availableQuantity = calculateAvailableQuantity(inventory);

        if (availableQuantity < request.quantity()) {
            log.warn(
                    "Insufficient stock: productId={}, requested={}, available={}",
                    request.productId(),
                    request.quantity(),
                    availableQuantity
            );
            return new ReserveResponse(
                    false,
                    availableQuantity,
                    String.format(
                            "Insufficient stock for product %d: requested %d, available %d",
                            request.productId(),
                            request.quantity(),
                            availableQuantity
                    )
            );
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);
        int availableAfterReservation = calculateAvailableQuantity(savedInventory);

        log.info(
                "Stock reserved: productId={}, reserved={}, available={}",
                savedInventory.getProductId(),
                request.quantity(),
                availableAfterReservation
        );
        return new ReserveResponse(
                true,
                availableAfterReservation,
                "Stock reserved successfully"
        );
    }

    private Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Inventory not found: productId={}", productId);
                    return new NotFoundException(String.format(
                            "Inventory for product %d was not found",
                            productId
                    ));
                });
    }

    private static InventoryDto toDto(Inventory inventory) {
        return InventoryMapper.toDto(inventory, calculateAvailableQuantity(inventory));
    }

    private static int calculateAvailableQuantity(Inventory inventory) {
        return inventory.getQuantity() - inventory.getReservedQuantity();
    }
}
