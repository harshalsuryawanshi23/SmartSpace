CREATE TABLE societies (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  name VARCHAR(160) NOT NULL,
  address_line VARCHAR(255) NOT NULL,
  locality VARCHAR(100) NOT NULL,
  city VARCHAR(80) NOT NULL,
  pincode CHAR(6) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  manager_user_id BIGINT UNSIGNED NOT NULL,
  registration_doc_path VARCHAR(255) NULL,
  verification_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  rejection_reason VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_soc_public (public_id),
  KEY ix_soc_manager (manager_user_id),
  CONSTRAINT fk_soc_manager FOREIGN KEY (manager_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE society_members (
  society_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  flat_label VARCHAR(30) NULL,
  status ENUM('PENDING','APPROVED','REMOVED') NOT NULL DEFAULT 'PENDING',
  approved_at DATETIME(3) NULL,
  PRIMARY KEY (society_id, user_id),
  CONSTRAINT fk_sm_soc FOREIGN KEY (society_id) REFERENCES societies(id) ON DELETE CASCADE,
  CONSTRAINT fk_sm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE halls (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  society_id BIGINT UNSIGNED NOT NULL,
  owner_user_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(160) NOT NULL,
  description TEXT NULL,
  address_line VARCHAR(255) NOT NULL,
  locality VARCHAR(100) NOT NULL,
  city VARCHAR(80) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  capacity_seated SMALLINT UNSIGNED NOT NULL,
  capacity_standing SMALLINT UNSIGNED NOT NULL,
  area_sqft SMALLINT UNSIGNED NULL,
  layout_type ENUM('OPEN_HALL','STAGE_HALL','COURTYARD','TERRACE','ROOM','MULTI_ROOM') NOT NULL DEFAULT 'OPEN_HALL',
  ceiling_height_ft DECIMAL(4,1) NULL,
  indoor TINYINT(1) NOT NULL DEFAULT 1,
  has_ac TINYINT(1) NOT NULL DEFAULT 0,
  has_parking TINYINT(1) NOT NULL DEFAULT 0,
  has_kitchen TINYINT(1) NOT NULL DEFAULT 0,
  has_stage TINYINT(1) NOT NULL DEFAULT 0,
  has_power_backup TINYINT(1) NOT NULL DEFAULT 0,
  has_washroom TINYINT(1) NOT NULL DEFAULT 1,
  power_points SMALLINT UNSIGNED NULL,
  rules_text TEXT NULL,
  base_price_per_hour DECIMAL(10,2) NOT NULL,
  min_slot_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 120,
  max_slot_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 240,
  buffer_after_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 30,      -- turnover/cleaning (exclusive cells)
  quiet_hours_start TIME NULL,                                     -- e.g. 22:00 (hall local time)
  quiet_hours_end TIME NULL,                                       -- e.g. 07:00
  latest_end_time TIME NULL,                                       -- e.g. 22:00
  advance_days_public SMALLINT UNSIGNED NOT NULL DEFAULT 30,
  advance_days_member SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  member_discount_percent DECIMAL(4,1) NOT NULL DEFAULT 0,
  cancellation_policy ENUM('FLEXIBLE','MODERATE','STRICT') NOT NULL DEFAULT 'MODERATE',
  overstay_fee_per_15min DECIMAL(10,2) NOT NULL DEFAULT 0,
  status ENUM('DRAFT','PENDING_APPROVAL','ACTIVE','SUSPENDED','REJECTED') NOT NULL DEFAULT 'DRAFT',
  rejection_reason VARCHAR(255) NULL,
  rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0,
  rating_count INT UNSIGNED NOT NULL DEFAULT 0,
  trust_score DECIMAL(5,2) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_hall_public (public_id),
  KEY ix_hall_status_geo (status, lat, lng),
  KEY ix_hall_owner (owner_user_id),
  KEY ix_hall_society (society_id),
  CONSTRAINT fk_hall_soc FOREIGN KEY (society_id) REFERENCES societies(id),
  CONSTRAINT fk_hall_owner FOREIGN KEY (owner_user_id) REFERENCES users(id),
  CONSTRAINT ck_hall_slot CHECK (min_slot_minutes % 30 = 0 AND max_slot_minutes % 30 = 0 AND min_slot_minutes <= max_slot_minutes),
  CONSTRAINT ck_hall_cap CHECK (capacity_seated > 0 AND capacity_standing >= capacity_seated)
) ENGINE=InnoDB;

CREATE TABLE hall_photos (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  caption VARCHAR(160) NULL,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_hp_hall (hall_id, sort_order),
  CONSTRAINT fk_hp_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_opening_hours (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  day_of_week TINYINT UNSIGNED NOT NULL,           -- 1=Mon ... 7=Sun (ISO)
  open_time TIME NOT NULL,                         -- hall-local
  close_time TIME NOT NULL,
  UNIQUE KEY uq_hoh (hall_id, day_of_week, open_time),
  CONSTRAINT fk_hoh_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
  CONSTRAINT ck_hoh CHECK (day_of_week BETWEEN 1 AND 7 AND open_time < close_time)
) ENGINE=InnoDB;

CREATE TABLE hall_blackouts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,                   -- UTC
  end_at DATETIME(3) NOT NULL,
  reason VARCHAR(160) NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  KEY ix_hb_hall (hall_id, start_at, end_at),
  CONSTRAINT fk_hb_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_price_rules (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  label VARCHAR(60) NOT NULL,                      -- "Weekend evening"
  days_mask TINYINT UNSIGNED NOT NULL,             -- bit0=Mon ... bit6=Sun
  from_time TIME NOT NULL,
  to_time TIME NOT NULL,
  price_per_hour DECIMAL(10,2) NOT NULL,
  priority SMALLINT NOT NULL DEFAULT 0,            -- higher wins
  active TINYINT(1) NOT NULL DEFAULT 1,
  KEY ix_hpr_hall (hall_id, active, priority),
  CONSTRAINT fk_hpr_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_staff (
  hall_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  staff_role ENUM('WATCHMAN','MANAGER') NOT NULL DEFAULT 'WATCHMAN',
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (hall_id, user_id),
  CONSTRAINT fk_hs_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
  CONSTRAINT fk_hs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
