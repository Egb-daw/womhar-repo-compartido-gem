package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla `equipments`.
 *
 * Representa un equipo físico/virtual instalado en un rack.
 * Relación:
 *  - N equipos pertenecen a 1 rack (N:1).
 *  - Puede tener 1:1 (PK compartida) con:
 *      - HostSpecification (solo servidores)
 *      - StorageBackup (NAS/STORAGE)
 *      - NetworkElement (SWITCH/ROUTER/FIREWALL)
 *  - Puede tener N logs (equipment_event_log)
 *  - Puede tener N órdenes de mantenimiento (maintenance_work_orders)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"rack", "hostSpecification", "storageBackup", "networkElement", "eventLogs", "workOrders"})
@Entity
@Table(
        name = "equipments",
        uniqueConstraints = @UniqueConstraint(name = "uq_equipments_serial", columnNames = "serial_number")
)
public class Equipment {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> racks.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rack_id", nullable = false)
    private Rack rack;

    /** VARCHAR(120) NOT NULL */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** ENUM(...) NOT NULL */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private EquipmentType type;

    /** VARCHAR(120) NOT NULL UNIQUE */
    @Column(name = "serial_number", nullable = false, length = 120, unique = true)
    private String serialNumber;

    /** VARCHAR(45) NULL */
    @Column(name = "primary_ip", length = 45)
    private String primaryIp;

    /** VARCHAR(45) NULL */
    @Column(name = "management_ip", length = 45)
    private String managementIp;

    /** INT NULL */
    @Column(name = "vlan_id")
    private Integer vlanId;

    /** VARCHAR(17) NULL */
    @Column(name = "mac_address", length = 17)
    private String macAddress;

    /** INT NULL (CHECK slot_position_u IS NULL OR > 0) */
    @Column(name = "slot_position_u")
    private Integer slotPositionU;

    /** INT NOT NULL DEFAULT 1 (CHECK > 0) */
    @Column(name = "slot_height_u", nullable = false)
    private Integer slotHeightU = 1;

    /** ENUM('ACTIVE','MAINTENANCE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP */
    @Column(name = "last_update", nullable = false, insertable = false, updatable = false)
    private LocalDateTime lastUpdate;

    /** 1:1 opcional (si existe fila en host_specifications) */
    @OneToOne(mappedBy = "equipment", fetch = FetchType.LAZY)
    private HostSpecification hostSpecification;

    /** 1:1 opcional (si existe fila en storage_backups) */
    @OneToOne(mappedBy = "equipment", fetch = FetchType.LAZY)
    private StorageBackup storageBackup;

    /** 1:1 opcional (si existe fila en network_elements) */
    @OneToOne(mappedBy = "equipment", fetch = FetchType.LAZY)
    private NetworkElement networkElement;

    /** Logs del equipo (puede haber muchos) */
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private Set<EquipmentEventLog> eventLogs = new HashSet<>();

    /** Órdenes de mantenimiento asociadas al equipo */
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private Set<MaintenanceWorkOrder> workOrders = new HashSet<>();
}
