CREATE TABLE customers (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(160) NOT NULL UNIQUE,
 contact_name VARCHAR(120),
 email VARCHAR(150),
 phone VARCHAR(40)
);

CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY,
 email VARCHAR(150) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL,
 name VARCHAR(120) NOT NULL,
 role VARCHAR(30) NOT NULL,
 customer_id BIGINT REFERENCES customers(id),
 active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sites (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(160) NOT NULL,
 address VARCHAR(255) NOT NULL,
 city VARCHAR(100),
 postal_code VARCHAR(30),
 customer_id BIGINT NOT NULL REFERENCES customers(id)
);

CREATE TABLE work_orders (
 id BIGSERIAL PRIMARY KEY,
 code VARCHAR(30) NOT NULL UNIQUE,
 title VARCHAR(180) NOT NULL,
 description VARCHAR(3000),
 priority VARCHAR(20) NOT NULL,
 status VARCHAR(20) NOT NULL,
 customer_id BIGINT NOT NULL REFERENCES customers(id),
 site_id BIGINT NOT NULL REFERENCES sites(id),
 assignee_id BIGINT REFERENCES users(id),
 sla_due_at TIMESTAMP NOT NULL,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_wo_status ON work_orders(status);
CREATE INDEX idx_wo_assignee ON work_orders(assignee_id);
CREATE INDEX idx_wo_customer ON work_orders(customer_id);

CREATE TABLE work_order_status_history (
 id BIGSERIAL PRIMARY KEY,
 work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
 from_status VARCHAR(20) NOT NULL,
 to_status VARCHAR(20) NOT NULL,
 changed_by BIGINT NOT NULL REFERENCES users(id),
 changed_at TIMESTAMP NOT NULL,
 note VARCHAR(1000)
);

CREATE TABLE parts (
 id BIGSERIAL PRIMARY KEY,
 sku VARCHAR(80) NOT NULL UNIQUE,
 name VARCHAR(180) NOT NULL,
 stock_quantity INT NOT NULL CHECK(stock_quantity >= 0),
 unit_price NUMERIC(12,2) NOT NULL CHECK(unit_price >= 0),
 reorder_level INT NOT NULL CHECK(reorder_level >= 0)
);

CREATE TABLE part_usages (
 id BIGSERIAL PRIMARY KEY,
 work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
 part_id BIGINT NOT NULL REFERENCES parts(id),
 quantity INT NOT NULL CHECK(quantity > 0),
 unit_price NUMERIC(12,2) NOT NULL
);

CREATE TABLE time_logs (
 id BIGSERIAL PRIMARY KEY,
 work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
 technician_id BIGINT NOT NULL REFERENCES users(id),
 minutes INT NOT NULL CHECK(minutes > 0),
 note VARCHAR(1000),
 logged_at TIMESTAMP NOT NULL
);

CREATE TABLE attachments (
 id BIGSERIAL PRIMARY KEY,
 work_order_id BIGINT NOT NULL REFERENCES work_orders(id),
 uploaded_by BIGINT NOT NULL REFERENCES users(id),
 original_name VARCHAR(255) NOT NULL,
 stored_name VARCHAR(255) NOT NULL,
 content_type VARCHAR(120) NOT NULL,
 size BIGINT NOT NULL,
 uploaded_at TIMESTAMP NOT NULL
);
