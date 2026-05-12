-- =====================================================================
-- data.sql (WomHat - MySQL)
-- =====================================================================

-- 1) CPDs
INSERT IGNORE INTO `data_centers` (`id`, `code`, `name`, `city`, `building`, `status`) VALUES
(1, 'CPD-SEV-01', 'CPD Principal WoMHAT', 'Sevilla', 'Edificio A', 'ACTIVE'),
(2, 'CPD-MAD-01', 'CPD Secundario WoMHAT', 'Madrid', 'Edificio C', 'ACTIVE');

-- 2) Salas
INSERT IGNORE INTO `data_center_rooms` (`id`, `data_center_id`, `name`, `floor`, `notes`) VALUES
(1, 1, 'Sala A (Producción)',      'S-1', 'Sala principal de producción.'),
(2, 1, 'Sala B (Red y Seguridad)', 'S-1', 'Core de red, firewalls y enlaces.'),
(3, 2, 'Sala A (DR)',              'S-2', 'Sala de contingencia / disaster recovery.');

-- 3) Racks
INSERT IGNORE INTO `racks`
(`id`, `room_id`, `location_label`, `capacity_u`, `function_name`, `group_name`, `dimension`, `position_x`, `position_y`, `status`, `catalog_visible`, `catalog_price`, `catalog_stock`, `catalog_summary`)
VALUES
(1, 1, 'CPD-SEV-01 / SalaA / Fila1-Rack01', 42, 'Servidores',     'PROD', '42U 600x1000', 10,  5, 'ACTIVE',      FALSE, NULL,    0, NULL),
(2, 1, 'CPD-SEV-01 / SalaA / Fila1-Rack02', 42, 'Almacenamiento', 'PROD', '42U 600x1000', 20,  5, 'ACTIVE',      TRUE, 1499.00, 0, 'Rack de almacenamiento listo para ampliaciones internas y despliegues controlados.'),
(3, 2, 'CPD-SEV-01 / SalaB / FilaR-Rack01', 42, 'Red',            'NET',  '42U 600x1000', 10, 15, 'ACTIVE',      TRUE,  899.00, 0, 'Rack de red con electrónica principal y margen de crecimiento para nuevas sedes.'),
(4, 3, 'CPD-MAD-01 / SalaA / Fila1-Rack01', 24, 'DR',             'DR',   '24U 600x800',  10,  5, 'ACTIVE',      TRUE, 1299.00, 1, 'Rack compacto de contingencia orientado a continuidad y recuperación operativa.');

-- 4) Usuarios
INSERT IGNORE INTO `users` (
  `id`, `email`, `password_hash`, `active`, `account_non_locked`,
  `last_password_change`, `password_expires_at`, `failed_login_attempts`,
  `email_verified`, `must_change_password`
) VALUES
(1, 'admin@app.local',      '$2a$12$HnF3pSI.kpCNujmMgcQDA.fbGt2TFPbmMDH.rT4wUKLvOKEzsvlTC', TRUE,  TRUE,  NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 0, TRUE,  FALSE),
(2, 'operaciones@app.local','$2a$12$HnF3pSI.kpCNujmMgcQDA.fbGt2TFPbmMDH.rT4wUKLvOKEzsvlTC', TRUE,  TRUE,  NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 1, TRUE,  FALSE),
(3, 'usuario@app.local',    '$2a$12$HnF3pSI.kpCNujmMgcQDA.fbGt2TFPbmMDH.rT4wUKLvOKEzsvlTC', TRUE,  TRUE,  NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 0, TRUE,  FALSE),
(4, 'bloqueado@app.local',  '$2a$12$HnF3pSI.kpCNujmMgcQDA.fbGt2TFPbmMDH.rT4wUKLvOKEzsvlTC', FALSE, FALSE, NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 5, FALSE, FALSE);

-- 5) Perfiles
INSERT IGNORE INTO `user_profiles`
(`user_id`,`first_name`,`last_name`,`phone_number`,`profile_image`,`bio`,`locale`)
VALUES
(1, 'David', 'Administrador', '+34 600 000 001', NULL, 'Administrador de la plataforma y responsable del backoffice técnico.', 'es_ES'),
(2, 'Laura', 'Operaciones',   '+34 600 000 002', NULL, 'Usuario operativo con trabajo diario sobre mantenimiento y catálogo interno.', 'es_ES'),
(3, 'María', 'Usuario',       '+34 600 000 003', NULL, 'Usuario final con acceso a catálogo interno, pedidos y racks asignados.', 'es_ES'),
(4, 'Pablo', 'Bloqueado',     '+34 600 000 004', NULL, 'Cuenta deshabilitada por intentos fallidos de acceso.', 'es_ES');

-- 6) Roles
INSERT IGNORE INTO `roles` (`id`, `name`, `display_name`, `description`) VALUES
(1, 'ROLE_ADMIN', 'Administrador', 'Acceso total al backoffice, mantenimiento, reportes y utilidades operativas.'),
(2, 'ROLE_USER',  'Usuario',       'Acceso al dashboard limitado, catálogo interno, pedidos y recursos asignados.');

-- 7) Relación user_roles
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 2);

-- 8) Acceso a racks
INSERT IGNORE INTO `user_rack_access`
(`user_id`, `rack_id`, `permission`, `original_owner`, `granted_by_user_id`)
VALUES
(1, 1, 'ADMIN', TRUE,  NULL),
(1, 2, 'ADMIN', FALSE, NULL),
(1, 3, 'ADMIN', FALSE, NULL),
(1, 4, 'ADMIN', FALSE, NULL),
(2, 1, 'WRITE', FALSE, 1),
(2, 2, 'WRITE', FALSE, 1),
(2, 3, 'ADMIN', TRUE,  1),
(2, 4, 'WRITE', FALSE, 1),
(3, 2, 'READ',  FALSE, 1),
(3, 4, 'READ',  FALSE, 1);

-- 9) Equipos
INSERT IGNORE INTO `equipments`
(`id`,`rack_id`,`name`,`type`,`serial_number`,`primary_ip`,`management_ip`,`vlan_id`,`mac_address`,`slot_position_u`,`slot_height_u`,`status`)
VALUES
(1, 1, 'Dell PowerEdge R740',           'SERVER',   'SRV-DEL-0001', '10.10.10.11', '10.10.99.11',  10, 'AA:BB:CC:DD:EE:01',  1, 2, 'ACTIVE'),
(2, 1, 'HP ProLiant DL380 Gen10',       'SERVER',   'SRV-HP-0002',  '10.10.10.12', '10.10.99.12',  10, 'AA:BB:CC:DD:EE:02',  3, 2, 'ACTIVE'),
(3, 2, 'Synology RS3617xs (NAS)',       'NAS',      'NAS-SYN-0003', '10.10.20.21', NULL,           20, 'AA:BB:CC:DD:EE:03',  1, 2, 'ACTIVE'),
(4, 2, 'Nodo TrueNAS (Almacenamiento)', 'STORAGE',  'STO-TRU-0004', '10.10.20.22', NULL,           20, 'AA:BB:CC:DD:EE:04',  3, 4, 'ACTIVE'),
(5, 3, 'Cisco Catalyst 9300',           'SWITCH',   'SW-CIS-0005',  '10.10.30.31', '10.10.99.31',  30, 'AA:BB:CC:DD:EE:05',  1, 1, 'ACTIVE'),
(6, 3, 'Ubiquiti UDM Pro',              'ROUTER',   'RT-UBI-0006',  '10.10.30.32', '10.10.99.32',  30, 'AA:BB:CC:DD:EE:06',  2, 1, 'ACTIVE'),
(7, 3, 'Fortinet FortiGate 60F',        'FIREWALL', 'FW-FOR-0007',  '10.10.30.33', '10.10.99.33',  30, 'AA:BB:CC:DD:EE:07',  3, 1, 'ACTIVE'),
(8, 4, 'APC Smart-UPS 3000',            'UPS',      'UPS-APC-0008', NULL,          NULL,          NULL,'AA:BB:CC:DD:EE:08',  1, 3, 'ACTIVE'),
(9, 4, 'Host DR Nodo 1',                'SERVER',   'SRV-DR-0009',  '10.20.10.11', '10.20.99.11', 110, 'AA:BB:CC:DD:EE:09',  5, 2, 'ACTIVE');

-- 10) Specs hosts (solo servers)
INSERT IGNORE INTO `host_specifications`
(`equipment_id`,`operating_system`,`purpose`,`install_date`,
 `cpu_architecture`,`cpu_model`,`cpu_cores`,`cpu_cache_mb`,`cpu_ghz`,
 `ram_type`,`ram_total_gb`,`ram_ghz`,
 `disk_total_gb`,`disk_read_mbps`,`disk_write_mbps`,
 `nic_count`,`nic_speed_mbps`,`notes`)
VALUES
(1, 'Ubuntu Server 22.04 LTS', 'Virtualización (Proxmox)', '2025-10-10',
 'x86_64','Intel Xeon Silver 4214R', 24, 165, 2.40,
 'DDR4', 128, 3.20,
 4000, 3500, 3000,
 4, 10000, 'Nodo principal de virtualización.'),
(2, 'Windows Server 2022', 'AD / DNS / Ficheros', '2025-11-05',
 'x86_64','Intel Xeon Gold 6230', 40, 275, 2.10,
 'DDR4', 256, 3.20,
 6000, 3200, 2800,
 2, 10000, 'Servicios de dominio y compartición.'),
(9, 'Debian 12', 'DR - Cómputo de respaldo', '2026-01-15',
 'x86_64','AMD EPYC 7302', 32, 256, 3.00,
 'DDR4', 128, 3.20,
 2000, 2500, 2000,
 2, 10000, 'Host de contingencia (DR).');

-- 11) Storage
INSERT IGNORE INTO `storage_backups`
(`equipment_id`,`distribution_type`,`install_date`,`storage_type`)
VALUES
(3, 'Synology DSM RAID-6', '2025-10-20', 'NAS'),
(4, 'ZFS RAIDZ2',          '2025-10-25', 'SAN');

-- 12) Red
INSERT IGNORE INTO `network_elements`
(`equipment_id`,`connection`,`total_ports`)
VALUES
(5, 'ETHERNET', 48),
(6, 'ETHERNET', 10),
(7, 'ETHERNET', 10);

-- 13) Pedidos de catálogo interno
INSERT IGNORE INTO `rack_purchase_orders`
(`id`, `user_id`, `rack_id`, `quantity`, `unit_price`, `total_price`, `status`, `notes`, `created_at`, `updated_at`)
VALUES
(1, 3, 2, 1, 1499.00, 1499.00, 'PLACED',    'Solicitud para ampliar el entorno de respaldo documental.', '2026-04-10 09:15:00', '2026-04-10 09:15:00'),
(2, 2, 3, 1,  899.00,  899.00, 'FULFILLED', 'Reposición de rack de red para laboratorio interno.',         '2026-04-08 11:00:00', '2026-04-09 13:30:00'),
(3, 3, 4, 1, 1299.00, 1299.00, 'CANCELLED', 'Pedido cancelado por cambio de prioridad del usuario.',      '2026-04-06 16:20:00', '2026-04-07 08:05:00');

-- 14) Token reset (ejemplo)
INSERT IGNORE INTO `password_reset_tokens`
(`id`,`user_id`,`token_hash`,`expires_at`,`used_at`,`created_at`,`request_ip`,`user_agent`)
VALUES
(1, 3, '0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef',
 DATE_ADD(NOW(), INTERVAL 45 MINUTE),
 NULL,
 NOW(),
 '203.0.113.15',
 'Mozilla/5.0 (X11; Linux x86_64)');

-- 15) Órdenes de Mantenimiento
INSERT IGNORE INTO `maintenance_work_orders`
(`id`, `equipment_id`, `created_by_user_id`, `status`, `priority`, `summary`, `details`, `opened_at`)
VALUES
(1, 1, 1, 'CLOSED', 'LOW', 'Mantenimiento preventivo trimestral', 'Limpieza y revisión de logs.', '2025-12-01 09:00:00'),
(2, 5, 2, 'OPEN', 'HIGH', 'Actualización de firmware', 'Parche de seguridad crítico.', NOW());

-- 16) Notas de Mantenimiento
INSERT IGNORE INTO `maintenance_notes` (`id`, `work_order_id`, `created_by_user_id`, `note`, `created_at`) VALUES
(1, 1, 1, 'Todo correcto, ventiladores limpios.', '2025-12-01 11:00:00');

-- 17) Log de eventos
INSERT IGNORE INTO `equipment_event_log`
(`id`, `equipment_id`, `changed_by_user_id`, `action`, `event_type`, `message`, `changed_at`)
VALUES
(99, 1, 1, 'UPDATE', 'STATUS_CHANGED', 'Cambio manual para test de carga', NOW()),
(100, 5, 2, 'UPDATE', 'UPDATED', 'Se actualiza inventario de puertos y electrónica asociada.', NOW()),
(101, 9, 2, 'UPDATE', 'CREATED', 'Alta inicial del host de contingencia en el CPD secundario.', NOW());
