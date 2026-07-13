ALTER TABLE services
    ADD COLUMN category VARCHAR(80) NULL,
    ADD COLUMN icon_key VARCHAR(64) NULL,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE bookings
    ADD COLUMN service_address VARCHAR(500) NULL,
    ADD COLUMN location_label VARCHAR(120) NULL,
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL;

ALTER TABLE images
    ADD COLUMN ai_detected_type VARCHAR(64) NULL,
    ADD COLUMN ai_label VARCHAR(160) NULL,
    ADD COLUMN ai_quantity INT NULL,
    ADD COLUMN ai_quantity_unit VARCHAR(32) NULL,
    ADD COLUMN ai_estimated_minutes INT NULL,
    ADD COLUMN ai_stain_level VARCHAR(32) NULL,
    ADD COLUMN ai_confidence DECIMAL(4, 3) NULL,
    ADD COLUMN ai_details_json TEXT NULL;

ALTER TABLE workers
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL;

UPDATE services SET
    category = 'Cleaning',
    icon_key = 'full-cleaning',
    sort_order = 1,
    name = 'Full home cleaning',
    description = 'Deep clean for entire home — rooms, kitchen, living areas.',
    base_price = 2499.00
WHERE id = 1;

UPDATE services SET
    category = 'Appliances',
    icon_key = 'ac',
    sort_order = 14,
    name = 'AC service & gas refill',
    description = 'Split/window AC inspection, jet cleaning, gas top-up.'
WHERE id = 2;

UPDATE services SET
    category = 'Repairs',
    icon_key = 'plumbing',
    sort_order = 15,
    name = 'Plumbing visit',
    description = 'Leaks, taps, blockages — on-site diagnosis.'
WHERE id = 3;

INSERT INTO services (name, description, base_price, category, icon_key, sort_order) VALUES
('Room cleaning', 'Bedroom or living room dusting, mopping, and surface wipe-down.', 899.00, 'Cleaning', 'room-cleaning', 2),
('Washroom cleaning', 'Toilet, tiles, taps, and bathroom sanitization.', 649.00, 'Cleaning', 'washroom', 3),
('Utensils washing', 'Sink full or piled utensils scrubbed and rinsed.', 299.00, 'Kitchen', 'utensils', 4),
('Dish washing', 'Plates, bowls, and cookware washed and dried.', 349.00, 'Kitchen', 'dishes', 5),
('Laundry', 'Clothes wash, dry, fold — per load.', 449.00, 'Home care', 'laundry', 6),
('Home keeping', 'Daily upkeep — tidy, dust, organize, light chores.', 799.00, 'Home care', 'homekeeping', 7),
('Cooking help', 'Meal prep, chopping, and kitchen assistance.', 699.00, 'Kitchen', 'cooking', 8),
('Painting touch-up', 'Wall patch, touch-up paint, and minor cosmetic fixes.', 1299.00, 'Repairs', 'painting', 9),
('Packing & movers', 'Box packing, loading, and move-day assistance.', 1999.00, 'Moving', 'packing', 10),
('Vehicle cleaning', 'Interior vacuum, exterior wash, and dashboard wipe.', 599.00, 'Outdoor', 'vehicle', 11),
('Hardware setup', 'Cot, furniture, wardrobe, and fixture assembly.', 899.00, 'Setup', 'hardware', 12),
('Appliance cleaning', 'Refrigerator, washing machine, TV area wipe-down.', 549.00, 'Appliances', 'appliance', 13);
