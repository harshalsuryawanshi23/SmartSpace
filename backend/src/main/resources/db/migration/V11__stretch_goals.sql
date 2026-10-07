-- V11__stretch_goals.sql

-- P3 stretch (N-10): Split-the-Cost
CREATE TABLE IF NOT EXISTS booking_cohosts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  phone VARCHAR(16) NULL,
  share_amount DECIMAL(10,2) NOT NULL,
  pay_token CHAR(32) NOT NULL,
  status ENUM('INVITED','PAID','EXPIRED') NOT NULL DEFAULT 'INVITED',
  paid_at DATETIME(3) NULL,
  UNIQUE KEY uq_cohost_token (pay_token),
  CONSTRAINT fk_cohost_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- P3 stretch (N-12): Web Push
CREATE TABLE IF NOT EXISTS push_subscriptions (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  endpoint VARCHAR(2048) NOT NULL,
  p256dh VARCHAR(255) NOT NULL,
  auth VARCHAR(255) NOT NULL,
  user_agent VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_push_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
