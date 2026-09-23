CREATE TABLE decorators (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    business_name VARCHAR(100) NOT NULL,
    lat DECIMAL(10, 8) NOT NULL,
    lng DECIMAL(11, 8) NOT NULL,
    service_radius_km DECIMAL(5, 2) NOT NULL,
    portfolio_urls JSON NULL,
    trust_score INT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'INACTIVE') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_decorator_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE decorator_packages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    decorator_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    event_types JSON NOT NULL, -- Array of event types or ["ANY"]
    theme_tags JSON NOT NULL, -- Array of tags e.g. ["balloon", "kids"]
    layout_types JSON NOT NULL, -- Array of layouts or ["ANY"]
    min_capacity INT NOT NULL,
    max_capacity INT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    price_per_guest DECIMAL(10, 2) NOT NULL DEFAULT 0,
    setup_minutes INT NOT NULL,
    teardown_minutes INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_package_decorator FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE decorator_blackouts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    decorator_id BIGINT UNSIGNED NOT NULL,
    start_time DATETIME(3) NOT NULL,
    end_time DATETIME(3) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_blackout_decorator FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE decorator_enquiries (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    decorator_id BIGINT UNSIGNED NOT NULL,
    package_id BIGINT UNSIGNED NOT NULL,
    message TEXT NULL,
    status ENUM('SENT', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'CONFIRMED_BY_RENTER') NOT NULL DEFAULT 'SENT',
    quoted_price DECIMAL(10, 2) NULL,
    note TEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_enquiry_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_enquiry_decorator FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE,
    CONSTRAINT fk_enquiry_package FOREIGN KEY (package_id) REFERENCES decorator_packages(id) ON DELETE CASCADE
) ENGINE=InnoDB;