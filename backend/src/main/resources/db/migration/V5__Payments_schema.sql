CREATE TABLE payments (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(20) NOT NULL,                   -- MOCK | RAZORPAY
  provider_order_id VARCHAR(80) NOT NULL,
  provider_payment_id VARCHAR(80) NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'INR',
  status ENUM('CREATED','AUTHORIZED','CAPTURED','FAILED','REFUNDED','PARTIALLY_REFUNDED') NOT NULL DEFAULT 'CREATED',
  signature_verified TINYINT(1) NOT NULL DEFAULT 0,
  failure_reason VARCHAR(160) NULL,
  raw_payload JSON NULL,                           -- sanitised gateway payload
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_pay_public (public_id),
  UNIQUE KEY uq_pay_order (provider, provider_order_id),
  UNIQUE KEY uq_pay_payment (provider, provider_payment_id),
  KEY ix_pay_booking (booking_id),
  CONSTRAINT fk_pay_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB;

CREATE TABLE refunds (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  payment_id BIGINT UNSIGNED NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  reason ENUM('RENTER_CANCEL','OWNER_CANCEL','ADMIN','DISPUTE') NOT NULL,
  provider_refund_id VARCHAR(80) NULL,
  status ENUM('PENDING','PROCESSED','FAILED') NOT NULL DEFAULT 'PENDING',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_ref_booking (booking_id),
  CONSTRAINT fk_ref_pay FOREIGN KEY (payment_id) REFERENCES payments(id)
) ENGINE=InnoDB;
