CREATE TABLE decorators (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  business_name VARCHAR(160) NOT NULL,
  description TEXT NULL,
  base_lat DECIMAL(9,6) NOT NULL,
  base_lng DECIMAL(9,6) NOT NULL,
  service_radius_km DECIMAL(5,1) NOT NULL DEFAULT 10,
  portfolio_paths JSON NULL,
  verification_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') NOT NULL DEFAULT 'PENDING',
  rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0,
  rating_count INT UNSIGNED NOT NULL DEFAULT 0,
  trust_score DECIMAL(5,2) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_dec_public (public_id),
  UNIQUE KEY uq_dec_user (user_id),
  KEY ix_dec_geo (verification_status, base_lat, base_lng),
  CONSTRAINT fk_dec_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE decorator_packages (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  decorator_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT NULL,
  event_types JSON NOT NULL,                       -- ["BIRTHDAY","BABY_SHOWER"] or ["ANY"]
  theme_tags JSON NOT NULL,                        -- ["balloon","floral","kids","traditional"]
  layout_types JSON NOT NULL,                      -- ["OPEN_HALL","STAGE_HALL"] or ["ANY"]
  min_capacity SMALLINT UNSIGNED NOT NULL,
  max_capacity SMALLINT UNSIGNED NOT NULL,
  base_price DECIMAL(10,2) NOT NULL,
  price_per_guest DECIMAL(8,2) NOT NULL DEFAULT 0,
  setup_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  teardown_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 30,
  active TINYINT(1) NOT NULL DEFAULT 1,
  KEY ix_dp_dec (decorator_id, active),
  CONSTRAINT fk_dp_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE,
  CONSTRAINT ck_dp_cap CHECK (min_capacity <= max_capacity)
) ENGINE=InnoDB;

CREATE TABLE decorator_blackouts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  decorator_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NOT NULL,
  reason VARCHAR(120) NULL,
  KEY ix_db_dec (decorator_id, start_at, end_at),
  CONSTRAINT fk_db_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE decorator_enquiries (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  decorator_id BIGINT UNSIGNED NOT NULL,
  package_id BIGINT UNSIGNED NULL,
  renter_user_id BIGINT UNSIGNED NOT NULL,
  message VARCHAR(1000) NULL,
  match_score DECIMAL(5,2) NULL,
  match_explanation JSON NULL,
  status ENUM('SENT','ACCEPTED','DECLINED','EXPIRED','CONFIRMED_BY_RENTER','CANCELLED') NOT NULL DEFAULT 'SENT',
  quoted_price DECIMAL(10,2) NULL,
  setup_window_start DATETIME(3) NULL,
  setup_window_end DATETIME(3) NULL,
  teardown_window_end DATETIME(3) NULL,
  responded_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_enq_public (public_id),
  KEY ix_enq_dec (decorator_id, status),
  KEY ix_enq_booking (booking_id),
  CONSTRAINT fk_enq_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT fk_enq_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id)
) ENGINE=InnoDB;
