CREATE TABLE IF NOT EXISTS products
(   id               UUID           PRIMARY KEY,
    product_name     VARCHAR(255)   NOT NULL,
    description      TEXT           NOT NULL,
    image_src        VARCHAR(2048),
    quantity_state   VARCHAR(32)    NOT NULL,
    product_state    VARCHAR(32)    NOT NULL,
    product_category VARCHAR(32),
    price            NUMERIC(19, 2) NOT NULL CHECK (price >= 1)
);