-- Create test database
CREATE DATABASE IF NOT EXISTS `smartspace_test`;
GRANT ALL PRIVILEGES ON `smartspace_test`.* TO 'smartspace'@'%';
FLUSH PRIVILEGES;
