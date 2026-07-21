CREATE TABLE IF NOT EXISTS warehouse_products (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    width DOUBLE PRECISION NOT NULL CHECK (width >= 1),
    height DOUBLE PRECISION NOT NULL CHECK (height >= 1),
    depth DOUBLE PRECISION NOT NULL CHECK (depth >= 1),
    weight DOUBLE PRECISION NOT NULL CHECK (weight >= 1),
    quantity BIGINT NOT NULL DEFAULT 0 CHECK (quantity >= 0)
);
