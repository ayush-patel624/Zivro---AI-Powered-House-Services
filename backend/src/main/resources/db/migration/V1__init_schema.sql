CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    phone VARCHAR(32),
    address VARCHAR(500)
);

CREATE TABLE workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    category VARCHAR(120),
    rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    verified BIT(1) NOT NULL DEFAULT 0,
    deposit_paid BIT(1) NOT NULL DEFAULT 0,
    emp_id VARCHAR(32) NOT NULL UNIQUE,
    CONSTRAINT fk_workers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(2000),
    base_price DECIMAL(12,2) NOT NULL
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    worker_id BIGINT,
    service_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    booking_time DATETIME(6) NOT NULL,
    urgency_level VARCHAR(32) NOT NULL,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_worker FOREIGN KEY (worker_id) REFERENCES workers (id),
    CONSTRAINT fk_bookings_service FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE TABLE ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    stars INT NOT NULL,
    feedback VARCHAR(2000),
    CONSTRAINT fk_ratings_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE TABLE images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    before_image VARCHAR(1024),
    after_image VARCHAR(1024),
    CONSTRAINT fk_images_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_bookings_worker ON bookings (worker_id);
CREATE INDEX idx_bookings_status ON bookings (status);
