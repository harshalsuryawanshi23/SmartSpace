CREATE TABLE users (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  phone VARCHAR(16) NOT NULL,                       -- E.164, e.g. +919876543210
  password_hash VARCHAR(100) NOT NULL,
  status ENUM('PENDING','ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'PENDING',
  email_verified_at DATETIME(3) NULL,
  phone_verified_at DATETIME(3) NULL,
  preferred_language ENUM('en','hi','mr') NOT NULL DEFAULT 'en',
  profile_photo_path VARCHAR(255) NULL,
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_users_public_id (public_id),
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT UNSIGNED NOT NULL,
  role ENUM('RESIDENT','HALL_OWNER','WATCHMAN','DECORATOR','ADMIN') NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,                     -- SHA-256 of opaque token
  family_id CHAR(36) NOT NULL,                      -- rotation family (reuse detection)
  expires_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  replaced_by BIGINT UNSIGNED NULL,
  user_agent VARCHAR(255) NULL,
  ip VARCHAR(45) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_refresh_hash (token_hash),
  KEY ix_refresh_user (user_id),
  KEY ix_refresh_family (family_id),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE otp_challenges (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  purpose ENUM('EMAIL_VERIFY','PHONE_VERIFY','PASSWORD_RESET','GATE_ENTRY','KYC') NOT NULL,
  user_id BIGINT UNSIGNED NULL,
  booking_id BIGINT UNSIGNED NULL,                  -- for GATE_ENTRY
  target VARCHAR(190) NOT NULL,                     -- email or phone (masked in logs)
  code_hash CHAR(64) NOT NULL,                      -- HMAC-SHA256(code, otp_pepper)
  attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  max_attempts TINYINT UNSIGNED NOT NULL DEFAULT 3,
  expires_at DATETIME(3) NOT NULL,
  verified_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_otp_public (public_id),
  KEY ix_otp_user_purpose (user_id, purpose, created_at),
  KEY ix_otp_booking (booking_id)
) ENGINE=InnoDB;

CREATE TABLE consents (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  purpose ENUM('KYC','LOCATION','MARKETING','TERMS','PRIVACY_POLICY') NOT NULL,
  policy_version VARCHAR(20) NOT NULL,
  granted_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  ip VARCHAR(45) NULL,
  KEY ix_consent_user (user_id, purpose),
  CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE kyc_verifications (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(30) NOT NULL,                    -- MOCK | AADHAAR_OFFLINE | DIGILOCKER | ...
  provider_ref VARCHAR(100) NULL,                   -- opaque reference from provider
  status ENUM('PENDING','VERIFIED','FAILED','EXPIRED','REVOKED') NOT NULL DEFAULT 'PENDING',
  verified_name VARCHAR(255) NULL,                  -- optionally AES-GCM encrypted
  masked_id VARCHAR(20) NULL,                       -- e.g. XXXX-XXXX-1234 (last 4 only, from provider)
  mobile_linked TINYINT(1) NOT NULL DEFAULT 0,      -- provider confirms the ID-linked mobile == users.phone
  consent_id BIGINT UNSIGNED NULL,
  verified_at DATETIME(3) NULL,
  expires_at DATETIME(3) NULL,
  failure_reason VARCHAR(120) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY ix_kyc_user (user_id, status),
  CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_kyc_consent FOREIGN KEY (consent_id) REFERENCES consents(id)
) ENGINE=InnoDB;
