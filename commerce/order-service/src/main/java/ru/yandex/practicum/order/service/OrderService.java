package ru.yandex.practicum.order.service;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.client.InventoryClient;
import ru.yandex.practicum.order.client.ProductClient;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.InventoryReserveRequestDto;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.ProductDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.exception.InactiveProductException;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient,
            InventoryClient inventoryClient
    ) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }

    public OrderDto createOrder(CreateOrderRequest request) {
        log.info(
                "Creating order: customerEmail={}, itemCount={}",
                request.customerEmail(),
                request.items().size()
        );

        List<OrderItem> items = enrichOrderItems(request.items());
        BigDecimal totalPrice = calculateTotalPrice(items);

        reserveStock(items);

        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setTotalPrice(totalPrice);

        for (OrderItem item : items) {
            order.addItem(item);
        }

        Order savedOrder = orderRepository.saveAndFlush(order);

        log.info(
                "Order created: id={}, customerEmail={}, totalPrice={}",
                savedOrder.getId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getTotalPrice()
        );
        return OrderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        log.debug("Getting all orders");

        List<OrderDto> orders = mapToDto(orderRepository.findAllByOrderByIdAsc());
        log.debug("Orders retrieved: count={}", orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        log.debug("Getting order: id={}", id);

        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Order not found: id={}", id);
                    return new NotFoundException(String.format("Order with id %d was not found", id));
                });
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByEmail(String email) {
        log.debug("Getting orders by customer email: email={}", email);

        List<OrderDto> orders = mapToDto(
                orderRepository.findByCustomerEmailIgnoreCaseOrderByIdAsc(email)
        );
        log.debug("Orders by customer email retrieved: email={}, count={}", email, orders.size());
        return orders;
    }

    private List<OrderItem> enrichOrderItems(List<OrderItemRequest> itemRequests) {
        List<OrderItem> items = new ArrayList<>(itemRequests.size());
        Map<Long, ProductDto> productCache = new HashMap<>();

        for (OrderItemRequest request : itemRequests) {
            Long productId = request.productId();

            if (!productCache.containsKey(productId)) {
                ProductDto product;
                try {
                    product = productClient.getProductById(productId);
                } catch (FeignException.NotFound e) {
                    log.warn("Product not found: productId={}", productId);
                    throw new NotFoundException(String.format(
                            "Product with id %d was not found",
                            productId
                    ), e);
                }

                if (!Boolean.TRUE.equals(product.active())) {
                    log.warn("Product is inactive: productId={}", productId);
                    throw new InactiveProductException(String.format(
                            "Product with id %d is inactive",
                            productId
                    ));
                }

                productCache.put(productId, product);
            }

            ProductDto product = productCache.get(productId);
            items.add(createOrderItemEntity(product, request.quantity()));
        }

        return items;
    }

    private static OrderItem createOrderItemEntity(ProductDto product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(product.id());
        item.setProductName(product.name());
        item.setQuantity(quantity);
        item.setPrice(product.price());
        return item;
    }

    private void reserveStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            inventoryClient.reserveStock(
                    new InventoryReserveRequestDto(item.getProductId(), item.getQuantity())
            );
        }
    }

    private static BigDecimal calculateTotalPrice(List<OrderItem> items) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItem item : items) {
            BigDecimal itemPrice = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);
        }

        return totalPrice;
    }

    private static List<OrderDto> mapToDto(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }
}
