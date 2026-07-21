CREATE TABLE IF NOT EXISTS shopping_carts (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS shopping_cart_items (
    id UUID PRIMARY KEY,
    shopping_cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_shopping_cart_items_cart
        FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT uq_shopping_cart_item_product
        UNIQUE (shopping_cart_id, product_id)
);
