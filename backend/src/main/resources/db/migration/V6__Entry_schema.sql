CREATE TABLE entry_credentials (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  jti CHAR(32) NOT NULL,                           -- random 128-bit hex, in QR payload
  booking_id BIGINT UNSIGNED NOT NULL,
  kind ENUM('HOLDER','DECORATOR') NOT NULL DEFAULT 'HOLDER',
  decorator_enquiry_id BIGINT UNSIGNED NULL,
  valid_from DATETIME(3) NOT NULL,
  valid_until DATETIME(3) NOT NULL,
  key_id VARCHAR(20) NOT NULL,
  revoked_at DATETIME(3) NULL,
  revoke_reason VARCHAR(120) NULL,
  issued_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_cred_jti (jti),
  KEY ix_cred_booking (booking_id, kind),
  CONSTRAINT fk_cred_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE entry_logs (                            -- append-only
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  client_event_id CHAR(36) NULL,                   -- offline idempotency (UUID from device)
  booking_id BIGINT UNSIGNED NULL,
  hall_id BIGINT UNSIGNED NOT NULL,
  credential_id BIGINT UNSIGNED NULL,
  watchman_user_id BIGINT UNSIGNED NULL,
  event_type ENUM('SCAN','OTP_SENT','OTP_VERIFIED','CHECK_IN','REENTRY','CHECK_OUT','HEADCOUNT','CAPACITY_ALERT',
                  'WRAP_UP_ALERT','OVERSTAY_ALERT','DECORATOR_IN','DECORATOR_OUT','SYNC_CONFLICT') NOT NULL,
  verdict ENUM('GO','HOLD','STOP') NULL,
  reason_code VARCHAR(40) NULL,
  identity_method ENUM('OTP','MANUAL_OFFLINE','NONE') NULL,
  offline TINYINT(1) NOT NULL DEFAULT 0,
  headcount SMALLINT UNSIGNED NULL,
  device_id VARCHAR(64) NULL,
  meta JSON NULL,
  occurred_at DATETIME(3) NOT NULL,                -- device time if offline, else server time
  recorded_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_el_client (client_event_id),
  KEY ix_el_booking (booking_id, occurred_at),
  KEY ix_el_hall (hall_id, occurred_at),
  CONSTRAINT fk_el_hall FOREIGN KEY (hall_id) REFERENCES halls(id)
) ENGINE=InnoDB;

CREATE TABLE hall_live_status (
  hall_id BIGINT UNSIGNED PRIMARY KEY,
  status ENUM('FREE','OCCUPIED','CLEANING') NOT NULL DEFAULT 'FREE',
  current_booking_id BIGINT UNSIGNED NULL,
  current_headcount SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_hls_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;
