-- One logical row per booking for workflow images (URLs + Cloudinary public ids)
ALTER TABLE images
    ADD COLUMN reference_image_url VARCHAR(1024) NULL AFTER booking_id,
    ADD COLUMN reference_public_id VARCHAR(255) NULL,
    ADD COLUMN before_public_id VARCHAR(255) NULL,
    ADD COLUMN after_public_id VARCHAR(255) NULL;

ALTER TABLE ratings
    ADD COLUMN satisfaction_stars INT NULL AFTER stars;

-- Enforce single image row per booking (application creates row with booking)
ALTER TABLE images ADD UNIQUE KEY uq_images_booking_id (booking_id);
