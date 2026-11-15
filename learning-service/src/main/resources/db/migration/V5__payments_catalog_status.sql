-- Payment gateway / transfer reference returned by the payment strategy (VNPay transaction no, bank transfer note)
ALTER TABLE customer_orders ADD COLUMN payment_reference VARCHAR(64);

-- Soft delete for products: orders keep their history, the course just stops being sold
ALTER TABLE courses ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- An order now produces one notification per status change (created, payment confirmed)
ALTER TABLE user_notifications DROP CONSTRAINT IF EXISTS user_notifications_order_id_key;
CREATE INDEX idx_user_notifications_order ON user_notifications(order_id);

-- Admin order list is sorted by newest first
CREATE INDEX idx_customer_orders_created ON customer_orders(created_at DESC);
