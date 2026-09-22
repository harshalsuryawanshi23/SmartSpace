ALTER TABLE hall_live_status ADD COLUMN capacity_alert_level VARCHAR(20) NULL;
ALTER TABLE bookings ADD COLUMN last_overstay_alert_at DATETIME(3) NULL;
