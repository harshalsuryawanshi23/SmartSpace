CREATE TABLE handover_reports (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  phase ENUM('BEFORE','AFTER') NOT NULL,
  checklist JSON NOT NULL,                         -- {"lightsOff":true,"acOff":true,"furnitureInPlace":true,"floorClean":false,"noDamage":true,"keysReturned":true}
  checklist_score DECIMAL(4,3) NOT NULL,           -- share of OK items, 0..1
  notes VARCHAR(1000) NULL,
  recorded_by BIGINT UNSIGNED NOT NULL,
  recorded_at DATETIME(3) NOT NULL,
  UNIQUE KEY uq_hr_phase (booking_id, phase),
  CONSTRAINT fk_hr_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE handover_photos (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  sha256 CHAR(64) NOT NULL,
  taken_at DATETIME(3) NOT NULL,
  caption VARCHAR(160) NULL,
  CONSTRAINT fk_hph_report FOREIGN KEY (report_id) REFERENCES handover_reports(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ratings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  rater_user_id BIGINT UNSIGNED NOT NULL,
  rater_side ENUM('RENTER','HALL_SIDE') NOT NULL,   -- HALL_SIDE = owner or watchman
  subject_type ENUM('HALL','RENTER','DECORATOR') NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,              -- hall.id / user.id / decorator.id
  stars TINYINT UNSIGNED NOT NULL,
  dimensions JSON NULL,                             -- {"cleanliness":5,"accuracy":4,"facilities":4,"helpfulness":5} or {"punctuality":5,"care":4,"conduct":5}
  comment VARCHAR(1000) NULL,
  weight DECIMAL(3,2) NOT NULL DEFAULT 1.00,        -- lowered by collusion check
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_rating (booking_id, rater_side, subject_type),
  KEY ix_rating_subject (subject_type, subject_id, created_at),
  CONSTRAINT fk_rating_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT ck_stars CHECK (stars BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE trust_scores (
  subject_type ENUM('RESIDENT','HALL','DECORATOR') NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  badge ENUM('NEW','STANDARD','TRUSTED','WATCH') NOT NULL,
  verified_stays INT UNSIGNED NOT NULL DEFAULT 0,
  components JSON NOT NULL,                        -- explainability payload
  computed_at DATETIME(3) NOT NULL,
  PRIMARY KEY (subject_type, subject_id)
) ENGINE=InnoDB;

CREATE TABLE disputes (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  raised_by BIGINT UNSIGNED NOT NULL,
  against_side ENUM('RENTER','HALL_SIDE') NOT NULL,
  category ENUM('DAMAGE','OVERSTAY','NO_ACCESS','MISREPRESENTED_LISTING','CLEANLINESS','OTHER') NOT NULL,
  description VARCHAR(2000) NOT NULL,
  claimed_amount DECIMAL(10,2) NULL,
  status ENUM('OPEN','UNDER_REVIEW','RESOLVED_FOR_RAISER','RESOLVED_AGAINST_RAISER','PARTIAL','WITHDRAWN') NOT NULL DEFAULT 'OPEN',
  resolution_note VARCHAR(2000) NULL,
  resolved_by BIGINT UNSIGNED NULL,
  resolved_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_disp_public (public_id),
  KEY ix_disp_booking (booking_id),
  KEY ix_disp_status (status, created_at),
  CONSTRAINT fk_disp_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB;

CREATE TABLE dispute_evidence (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dispute_id BIGINT UNSIGNED NOT NULL,
  submitted_by BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NULL,
  sha256 CHAR(64) NULL,
  note VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_de_disp FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
) ENGINE=InnoDB;
