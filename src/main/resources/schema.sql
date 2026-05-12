-- =====================================================================
-- schema.sql (WomHat - MySQL)
-- =====================================================================

CREATE TABLE IF NOT EXISTS `data_centers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(30) NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `city` VARCHAR(80) NULL,
  `building` VARCHAR(120) NULL,
  `status` ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_data_centers_code` (`code`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `data_center_rooms` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `data_center_id` BIGINT NOT NULL,
  `name` VARCHAR(80) NOT NULL,
  `floor` VARCHAR(20) NULL,
  `notes` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rooms_dc` (`data_center_id`),
  CONSTRAINT `fk_rooms_data_center`
    FOREIGN KEY (`data_center_id`)
    REFERENCES `data_centers` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `racks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `room_id` BIGINT NOT NULL,
    `location_label` VARCHAR(150) NOT NULL,
    `capacity_u` INT NOT NULL,
    `function_name` VARCHAR(100) NOT NULL,
    `group_name` VARCHAR(60) NULL,
    `dimension` VARCHAR(60) NULL,
    `position_x` INT NOT NULL DEFAULT 0,
    `position_y` INT NOT NULL DEFAULT 0,
    `status` ENUM('ACTIVE','MAINTENANCE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `catalog_visible` BOOLEAN NOT NULL DEFAULT FALSE,
    `catalog_price` DECIMAL(10,2) NULL,
    `catalog_stock` INT NOT NULL DEFAULT 0,
    `catalog_summary` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_racks_location_label` (`location_label`),
    KEY `idx_racks_room` (`room_id`),
    KEY `idx_racks_catalog_visible` (`catalog_visible`),
    CONSTRAINT `chk_racks_capacity_u` CHECK (`capacity_u` > 0),
    CONSTRAINT `chk_racks_catalog_stock` CHECK (`catalog_stock` >= 0),
    CONSTRAINT `chk_racks_catalog_price` CHECK (`catalog_price` IS NULL OR `catalog_price` >= 0),
    CONSTRAINT `fk_racks_room`
    FOREIGN KEY (`room_id`)
    REFERENCES `data_center_rooms` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `equipments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rack_id` BIGINT NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `type` ENUM('SERVER','SWITCH','ROUTER','NAS','UPS','STORAGE','FIREWALL','OTHER') NOT NULL,
  `serial_number` VARCHAR(120) NOT NULL,
  `primary_ip` VARCHAR(45) NULL,
  `management_ip` VARCHAR(45) NULL COMMENT 'IP for iDRAC/iLO/SSH',
  `vlan_id` INT NULL,
  `mac_address` VARCHAR(17) NULL,
  `slot_position_u` INT NULL,
  `slot_height_u` INT NOT NULL DEFAULT 1,
  `status` ENUM('ACTIVE','MAINTENANCE','INACTIVE','RETIRED','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  `last_update` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_equipments_serial` (`serial_number`),
  KEY `idx_equipments_rack` (`rack_id`),
  KEY `idx_equipments_type` (`type`),
  CONSTRAINT `chk_equipments_slot_height` CHECK (`slot_height_u` > 0),
  CONSTRAINT `chk_equipments_slot_position` CHECK (`slot_position_u` IS NULL OR `slot_position_u` > 0),
  CONSTRAINT `fk_equipments_rack`
    FOREIGN KEY (`rack_id`)
    REFERENCES `racks` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL,
  `password_hash` VARCHAR(500) NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `account_non_locked` BOOLEAN NOT NULL DEFAULT TRUE,
  `last_password_change` DATETIME NULL,
  `password_expires_at` DATETIME NULL,
  `failed_login_attempts` INT NOT NULL DEFAULT 0,
  `email_verified` BOOLEAN NOT NULL DEFAULT FALSE,
  `must_change_password` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  KEY `idx_users_active` (`active`),
  KEY `idx_users_locked` (`account_non_locked`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_profiles` (
  `user_id` BIGINT NOT NULL,
  `first_name` VARCHAR(60) NOT NULL,
  `last_name` VARCHAR(80) NOT NULL,
  `phone_number` VARCHAR(30) NULL,
  `profile_image` VARCHAR(255) NULL,
  `bio` VARCHAR(500) NULL,
  `locale` VARCHAR(10) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_profiles_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `display_name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_name` (`name`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_user_roles_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_roles_role`
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token_hash` VARCHAR(64) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `used_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `request_ip` VARCHAR(45) NULL,
  `user_agent` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_prt_user_id` (`user_id`),
  KEY `idx_prt_token_hash` (`token_hash`),
  KEY `idx_prt_expires_at` (`expires_at`),
  CONSTRAINT `fk_prt_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_rack_access` (
  `user_id` BIGINT NOT NULL,
  `rack_id` BIGINT NOT NULL,
  `permission` ENUM('READ','WRITE','ADMIN') NOT NULL DEFAULT 'READ',
  `original_owner` BOOLEAN NOT NULL DEFAULT FALSE,
  `granted_by_user_id` BIGINT NULL,
  `granted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `rack_id`),
  KEY `idx_ura_rack` (`rack_id`),
  CONSTRAINT `fk_ura_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ura_rack`
    FOREIGN KEY (`rack_id`) REFERENCES `racks` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `rack_purchase_orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `rack_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` ENUM('PLACED','FULFILLED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_rpo_user` (`user_id`),
    KEY `idx_rpo_rack` (`rack_id`),
    KEY `idx_rpo_status` (`status`),
    CONSTRAINT `chk_rpo_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_rpo_unit_price` CHECK (`unit_price` >= 0),
    CONSTRAINT `chk_rpo_total_price` CHECK (`total_price` >= 0),
    CONSTRAINT `fk_rpo_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_rpo_rack`
    FOREIGN KEY (`rack_id`)
        REFERENCES `racks` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `host_specifications` (
  `equipment_id` BIGINT NOT NULL,
  `operating_system` VARCHAR(120) NULL,
  `purpose` VARCHAR(120) NULL,
  `install_date` DATE NULL,
  `cpu_architecture` VARCHAR(80) NULL,
  `cpu_model` VARCHAR(120) NULL,
  `cpu_cores` INT NULL,
  `cpu_cache_mb` INT NULL,
  `cpu_ghz` DECIMAL(6,2) NULL,
  `ram_type` VARCHAR(60) NULL,
  `ram_total_gb` INT NULL,
  `ram_ghz` DECIMAL(6,2) NULL,
  `disk_total_gb` INT NULL,
  `disk_read_mbps` INT NULL,
  `disk_write_mbps` INT NULL,
  `nic_count` INT NULL,
  `nic_speed_mbps` INT NULL,
  `notes` VARCHAR(255) NULL,
  PRIMARY KEY (`equipment_id`),
  CONSTRAINT `chk_cpu_cores` CHECK (`cpu_cores` IS NULL OR `cpu_cores` > 0),
  CONSTRAINT `chk_cpu_cache` CHECK (`cpu_cache_mb` IS NULL OR `cpu_cache_mb` >= 0),
  CONSTRAINT `chk_ram_total` CHECK (`ram_total_gb` IS NULL OR `ram_total_gb` >= 0),
  CONSTRAINT `chk_disk_total` CHECK (`disk_total_gb` IS NULL OR `disk_total_gb` >= 0),
  CONSTRAINT `chk_nic_count` CHECK (`nic_count` IS NULL OR `nic_count` >= 0),
  CONSTRAINT `fk_hostspec_equipment`
    FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `storage_backups` (
  `equipment_id` BIGINT NOT NULL,
  `distribution_type` VARCHAR(120) NULL,
  `install_date` DATE NULL,
  `storage_type` ENUM('NAS','SAN','BACKUP','OBJECT','OTHER') NOT NULL DEFAULT 'BACKUP',
  PRIMARY KEY (`equipment_id`),
  CONSTRAINT `fk_storage_equipment`
    FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `network_elements` (
  `equipment_id` BIGINT NOT NULL,
  `connection` ENUM('ETHERNET','FIBER','WIFI','OTHER') NOT NULL DEFAULT 'ETHERNET',
  `total_ports` INT NULL,
  PRIMARY KEY (`equipment_id`),
  CONSTRAINT `chk_net_total_ports` CHECK (`total_ports` IS NULL OR `total_ports` >= 0),
  CONSTRAINT `fk_network_equipment`
    FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `equipment_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT NULL,
  `changed_by_user_id` BIGINT NULL,
  `action` ENUM('INSERT','UPDATE','DELETE') NOT NULL,
  `event_type` ENUM('CREATED','UPDATED','MOVED_RACK','STATUS_CHANGED','DELETED') NOT NULL,
  `old_rack_id` BIGINT NULL,
  `new_rack_id` BIGINT NULL,
  `old_status` VARCHAR(30) NULL,
  `new_status` VARCHAR(30) NULL,
  `message` VARCHAR(255) NULL,
  `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event_equipment` (`equipment_id`),
  KEY `idx_event_changed_by` (`changed_by_user_id`),
  CONSTRAINT `fk_event_equipment`
    FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_event_user`
    FOREIGN KEY (`changed_by_user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `maintenance_work_orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT NOT NULL,
  `created_by_user_id` BIGINT NULL,
  `status` ENUM('OPEN','IN_PROGRESS','CLOSED') NOT NULL DEFAULT 'OPEN',
  `priority` ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
  `summary` VARCHAR(180) NOT NULL,
  `details` TEXT NULL,
  `opened_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `closed_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wo_equipment` (`equipment_id`),
  KEY `idx_wo_status` (`status`),
  CONSTRAINT `fk_wo_equipment`
    FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wo_user`
    FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `maintenance_notes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `work_order_id` BIGINT NOT NULL,
  `created_by_user_id` BIGINT NULL,
  `note` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_note_wo` (`work_order_id`),
  CONSTRAINT `fk_note_wo`
    FOREIGN KEY (`work_order_id`) REFERENCES `maintenance_work_orders` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_note_user`
    FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;
