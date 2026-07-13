ALTER TABLE bookings
    ADD COLUMN razorpay_deposit_payment_id VARCHAR(80) NULL,
    ADD COLUMN razorpay_balance_payment_id VARCHAR(80) NULL;
