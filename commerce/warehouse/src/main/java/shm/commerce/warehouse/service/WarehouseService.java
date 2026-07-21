package shm.commerce.warehouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shm.commerce.interactionapi.dto.AddProductToWarehouseRequest;
import shm.commerce.interactionapi.dto.AddressDto;
import shm.commerce.interactionapi.dto.BookedProductsDto;
import shm.commerce.interactionapi.dto.DimensionDto;
import shm.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import shm.commerce.interactionapi.dto.ShoppingCartDto;
import shm.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import shm.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import shm.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import shm.commerce.warehouse.exception.WarehouseValidationException;
import shm.commerce.warehouse.model.WarehouseProduct;
import shm.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);
    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;

    public WarehouseService(WarehouseProductRepository warehouseProductRepository) {
        this.warehouseProductRepository = warehouseProductRepository;
    }

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        UUID productId = request.getProductId();
        if (warehouseProductRepository.existsByProductId(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }

        DimensionDto dimension = request.getDimension();
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(productId);
        product.setFragile(Boolean.TRUE.equals(request.getFragile()));
        product.setWidth(dimension.getWidth());
        product.setHeight(dimension.getHeight());
        product.setDepth(dimension.getDepth());
        product.setWeight(request.getWeight());
        product.setQuantity(0L);

        warehouseProductRepository.save(product);
        log.info("Product registered in warehouse: productId={}, fragile={}, weight={}",
                productId, product.isFragile(), product.getWeight());
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        UUID productId = request.getProductId();
        if (productId == null) {
            throw new WarehouseValidationException("Product id must not be null.");
        }

        WarehouseProduct product = warehouseProductRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

        long previousQuantity = product.getQuantity();
        product.setQuantity(previousQuantity + request.getQuantity());
        warehouseProductRepository.save(product);

        log.info("Warehouse quantity increased: productId={}, added={}, previous={}, current={}",
                productId, request.getQuantity(), previousQuantity, product.getQuantity());
    }

    @Transactional
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        if (shoppingCart == null) {
            throw new WarehouseValidationException("Shopping cart must not be null.");
        }

        Map<UUID, Long> requestedProducts = shoppingCart.getProducts();
        if (requestedProducts == null) {
            throw new WarehouseValidationException("Shopping cart products must not be null.");
        }

        log.debug("Checking warehouse stock: shoppingCartId={}, productCount={}",
                shoppingCart.getShoppingCartId(), requestedProducts.size());

        Map<UUID, WarehouseProduct> warehouseProducts = new HashMap<>();
        List<String> shortages = new ArrayList<>();

        // Сначала проверяем и загружаем все запрошенные товары, не изменяя остатки.
        // Это исключает частичное резервирование, если один из следующих товаров
        // отсутствует на складе или его количества недостаточно.
        for (Map.Entry<UUID, Long> entry : requestedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            if (productId == null) {
                throw new WarehouseValidationException("Product id in shopping cart must not be null.");
            }
            if (requestedQuantity == null || requestedQuantity < 1) {
                throw new WarehouseValidationException(
                        "Requested quantity for product " + productId + " must be greater than zero."
                );
            }

            WarehouseProduct product = warehouseProductRepository.findByProductId(productId)
                    .orElse(null);
            warehouseProducts.put(productId, product);

            long availableQuantity = product == null ? 0L : product.getQuantity();
            if (availableQuantity < requestedQuantity) {
                shortages.add(
                        "productId=" + productId
                                + ", requested=" + requestedQuantity
                                + ", available=" + availableQuantity
                );
            }
        }

        if (!shortages.isEmpty()) {
            log.warn("Warehouse reservation rejected: shoppingCartId={}, shortages={}",
                    shoppingCart.getShoppingCartId(), shortages);
            throw new ProductInShoppingCartLowQuantityInWarehouseException(
                    "Not enough products in warehouse: " + String.join("; ", shortages)
            );
        }

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;
        List<WarehouseProduct> productsToSave = new ArrayList<>();

        // Только после успешной проверки всего запроса резервируем товары
        // и за один проход рассчитываем характеристики доставки.
        for (Map.Entry<UUID, Long> entry : requestedProducts.entrySet()) {
            WarehouseProduct product = warehouseProducts.get(entry.getKey());
            long requestedQuantity = entry.getValue();

            product.setQuantity(product.getQuantity() - requestedQuantity);
            productsToSave.add(product);

            deliveryWeight += product.getWeight() * requestedQuantity;
            deliveryVolume += product.getWidth()
                    * product.getHeight()
                    * product.getDepth()
                    * requestedQuantity;
            fragile = fragile || product.isFragile();
        }

        warehouseProductRepository.saveAll(productsToSave);

        BookedProductsDto bookedProducts = new BookedProductsDto();
        bookedProducts.setDeliveryWeight(deliveryWeight);
        bookedProducts.setDeliveryVolume(deliveryVolume);
        bookedProducts.setFragile(fragile);

        log.info("Warehouse products reserved: shoppingCartId={}, productCount={}, weight={}, volume={}, fragile={}",
                shoppingCart.getShoppingCartId(), requestedProducts.size(),
                deliveryWeight, deliveryVolume, fragile);
        return bookedProducts;
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        AddressDto address = new AddressDto();
        address.setCountry(CURRENT_ADDRESS);
        address.setCity(CURRENT_ADDRESS);
        address.setStreet(CURRENT_ADDRESS);
        address.setHouse(CURRENT_ADDRESS);
        address.setFlat(CURRENT_ADDRESS);

        log.debug("Warehouse address requested: address={}", CURRENT_ADDRESS);
        return address;
    }
}
