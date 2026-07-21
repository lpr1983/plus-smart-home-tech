package shm.commerce.shoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import shm.commerce.interactionapi.client.WarehouseClient;

@SpringBootApplication
@EnableFeignClients(clients = WarehouseClient.class)
public class ShoppingCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingCartApplication.class, args);
    }
}
