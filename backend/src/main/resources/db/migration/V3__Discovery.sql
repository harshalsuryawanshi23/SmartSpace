CREATE TABLE localities (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  city VARCHAR(80) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  KEY ix_loc_name (name)
) ENGINE=InnoDB;

INSERT INTO localities (name, city, lat, lng) VALUES
('Koregaon Park', 'Pune', 18.536208, 73.893975),
('Viman Nagar', 'Pune', 18.567915, 73.914343),
('Kalyani Nagar', 'Pune', 18.548292, 73.901597),
('Baner', 'Pune', 18.559000, 73.786800),
('Aundh', 'Pune', 18.563500, 73.805500),
('Shivajinagar', 'Pune', 18.531400, 73.844600),
('Kothrud', 'Pune', 18.507400, 73.807700),
('Hinjewadi', 'Pune', 18.591300, 73.738900),
('Wakad', 'Pune', 18.598700, 73.768800),
('Magarpatta', 'Pune', 18.512600, 73.926000);

CREATE TABLE bookings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  renter_user_id BIGINT UNSIGNED NOT NULL,
  hall_id BIGINT UNSIGNED NOT NULL,
  status ENUM('PENDING_PAYMENT','CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'PENDING_PAYMENT',
  lock_expires_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_booking_public (public_id),
  KEY ix_booking_hall (hall_id)
) ENGINE=InnoDB;

CREATE TABLE booking_cells (
  booking_id BIGINT UNSIGNED NOT NULL,
  hall_id BIGINT UNSIGNED NOT NULL,
  cell_start DATETIME(3) NOT NULL,
  PRIMARY KEY (hall_id, cell_start),
  KEY ix_bc_booking (booking_id),
  CONSTRAINT fk_bc_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_bc_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;
