ALTER TABLE bookings
    ADD COLUMN payment_status VARCHAR(32) NOT NULL DEFAULT 'PAID',
    ADD COLUMN razorpay_order_id VARCHAR(80) NULL,
    ADD COLUMN razorpay_balance_order_id VARCHAR(80) NULL,
    ADD COLUMN razorpay_payment_id VARCHAR(80) NULL,
    ADD COLUMN deposit_amount DECIMAL(12,2) NULL,
    ADD COLUMN amount_paid DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN final_price_after_satisfaction DECIMAL(12,2) NULL;

UPDATE bookings SET amount_paid = price WHERE amount_paid = 0;

CREATE TABLE disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(2000) NOT NULL,
    resolution_notes VARCHAR(4000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_disputes_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

CREATE INDEX idx_disputes_status ON disputes (status);
CREATE INDEX idx_disputes_booking ON disputes (booking_id);
