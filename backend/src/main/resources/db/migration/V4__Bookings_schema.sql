DROP TABLE IF EXISTS booking_cells;
DROP TABLE IF EXISTS bookings;

CREATE TABLE bookings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_ref VARCHAR(20) NOT NULL,                -- SS-2610-7K3QF
  hall_id BIGINT UNSIGNED NOT NULL,
  renter_user_id BIGINT UNSIGNED NOT NULL,
  event_type ENUM('BIRTHDAY','KITTY_PARTY','MEETING','TUITION_BATCH','FESTIVAL','GET_TOGETHER','BABY_SHOWER','WORKSHOP','OTHER') NOT NULL,
  event_title VARCHAR(160) NOT NULL,
  theme_tags JSON NULL,                            -- ["balloon","kids"]
  guest_count SMALLINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,                   -- UTC, aligned to :00/:30
  end_at DATETIME(3) NOT NULL,
  status ENUM('PENDING_PAYMENT','CONFIRMED','CHECKED_IN','CHECKED_OUT','COMPLETED','CANCELLED','EXPIRED','NO_SHOW') NOT NULL,
  lock_expires_at DATETIME(3) NULL,
  price_base DECIMAL(10,2) NOT NULL,
  price_member_discount DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_platform_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_tax DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_total DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'INR',
  is_member_booking TINYINT(1) NOT NULL DEFAULT 0,
  cancellation_policy ENUM('FLEXIBLE','MODERATE','STRICT') NOT NULL,   -- snapshot at booking time
  cancelled_by ENUM('RENTER','OWNER','ADMIN','SYSTEM') NULL,
  cancel_reason VARCHAR(255) NULL,
  cancelled_at DATETIME(3) NULL,
  checked_in_at DATETIME(3) NULL,
  checked_out_at DATETIME(3) NULL,
  arrived_headcount SMALLINT UNSIGNED NULL,
  peak_headcount SMALLINT UNSIGNED NULL,
  overstay_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  dispute_open TINYINT(1) NOT NULL DEFAULT 0,
  rating_window_closes_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,                  -- optimistic locking
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_booking_public (public_id),
  UNIQUE KEY uq_booking_ref (booking_ref),
  KEY ix_booking_hall_time (hall_id, start_at),
  KEY ix_booking_renter (renter_user_id, start_at),
  KEY ix_booking_status_lock (status, lock_expires_at),
  KEY ix_booking_status_start (status, start_at),
  CONSTRAINT fk_booking_hall FOREIGN KEY (hall_id) REFERENCES halls(id),
  CONSTRAINT fk_booking_renter FOREIGN KEY (renter_user_id) REFERENCES users(id),
  CONSTRAINT ck_booking_time CHECK (end_at > start_at)
) ENGINE=InnoDB;

-- THE double-booking guarantee: one row per hall x 30-minute cell.
CREATE TABLE booking_cells (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  cell_start DATETIME(0) NOT NULL,                 -- UTC, minute in (0,30), second 0
  booking_id BIGINT UNSIGNED NOT NULL,
  cell_type ENUM('BOOKED','BUFFER','SETUP') NOT NULL DEFAULT 'BOOKED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_cell (hall_id, cell_start),
  KEY ix_cell_booking (booking_id),
  CONSTRAINT fk_bc_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_bc_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
  CONSTRAINT ck_cell_aligned CHECK (MINUTE(cell_start) IN (0,30) AND SECOND(cell_start) = 0)
) ENGINE=InnoDB;

CREATE TABLE booking_events (                       -- append-only audit trail
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  event_type ENUM('CREATED','PAID','QR_ISSUED','CHECKED_IN','CHECKED_OUT','RATED_BY_RENTER','RATED_BY_OWNER','CANCELLED','REFUNDED','EXPIRED','NO_SHOW') NOT NULL,
  actor_type ENUM('RENTER','OWNER','WATCHMAN','ADMIN','SYSTEM') NOT NULL,
  actor_id BIGINT UNSIGNED NULL,
  details JSON NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_be_booking (booking_id),
  CONSTRAINT fk_be_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE idempotency_keys (
  idempotency_key VARCHAR(100) PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  request_path VARCHAR(200) NOT NULL,
  locked_at DATETIME(3) NOT NULL,
  response_status INT NULL,
  response_body JSON NULL,
  completed_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_idem_user_time (user_id, created_at)
) ENGINE=InnoDB;
