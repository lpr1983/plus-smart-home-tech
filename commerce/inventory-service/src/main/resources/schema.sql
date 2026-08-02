CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_inventory_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_reserved_non_negative CHECK (reserved_quantity >= 0)
);
