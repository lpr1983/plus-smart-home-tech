package shm.commerce.warehouse.controller;

import org.springframework.web.bind.annotation.RestController;
import shm.commerce.interactionapi.client.WarehouseClient;
import shm.commerce.interactionapi.dto.AddProductToWarehouseRequest;
import shm.commerce.interactionapi.dto.AddressDto;
import shm.commerce.interactionapi.dto.BookedProductsDto;
import shm.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import shm.commerce.interactionapi.dto.ShoppingCartDto;
import shm.commerce.warehouse.service.WarehouseService;

@RestController
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCart);
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}
