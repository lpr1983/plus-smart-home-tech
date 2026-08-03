package ru.yandex.practicum.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info(
                "Creating order: customerEmail={}, itemCount={}",
                request.customerEmail(),
                request.items().size()
        );

        BigDecimal totalPrice = calculateTotalPrice(request.items());
        Order order = OrderMapper.toEntity(request, totalPrice);
        Order savedOrder = orderRepository.saveAndFlush(order);

        log.info(
                "Order created: id={}, customerEmail={}, totalPrice={}",
                savedOrder.getId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getTotalPrice()
        );
        return OrderMapper.toDto(savedOrder);
    }

    public List<OrderDto> getAllOrders() {
        log.debug("Getting all orders");

        List<OrderDto> orders = mapToDto(orderRepository.findAllByOrderByIdAsc());
        log.debug("Orders retrieved: count={}", orders.size());
        return orders;
    }

    public OrderDto getOrderById(Long id) {
        log.debug("Getting order: id={}", id);

        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Order not found: id={}", id);
                    return new NotFoundException(String.format("Order with id %d was not found", id));
                });
    }

    public List<OrderDto> getOrdersByEmail(String email) {
        log.debug("Getting orders by customer email: email={}", email);

        List<OrderDto> orders = mapToDto(
                orderRepository.findByCustomerEmailIgnoreCaseOrderByIdAsc(email)
        );
        log.debug("Orders by customer email retrieved: email={}, count={}", email, orders.size());
        return orders;
    }

    private static BigDecimal calculateTotalPrice(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<OrderDto> mapToDto(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }
}
