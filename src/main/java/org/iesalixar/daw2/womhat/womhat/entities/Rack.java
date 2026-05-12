package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla `racks`.
 *
 * Representa un rack dentro de una sala.
 * Relación:
 *  - Muchos racks pertenecen a una sala (N:1).
 *  - Un rack tiene muchos equipos (1:N).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"room", "equipments", "userAccess"})
@Entity
@Table(
        name = "racks",
        uniqueConstraints = @UniqueConstraint(name = "uq_racks_location_label", columnNames = "location_label")
)
public class Rack {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> data_center_rooms.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private DataCenterRoom room;

    /** VARCHAR(150) NOT NULL UNIQUE */
    @Column(name = "location_label", nullable = false, length = 150)
    private String locationLabel;

    /**
     * INT NOT NULL (CHECK capacity_u > 0)
     * La validación real en BD ya existe.
     */
    @Column(name = "capacity_u", nullable = false)
    private Integer capacityU;

    /** VARCHAR(100) NOT NULL */
    @Column(name = "function_name", nullable = false, length = 100)
    private String functionName;

    /** VARCHAR(60) NULL */
    @Column(name = "group_name", length = 60)
    private String groupName;

    /** VARCHAR(60) NULL */
    @Column(name = "dimension", length = 60)
    private String dimension;

    /** INT NOT NULL DEFAULT 0 */
    @Column(name = "position_x", nullable = false)
    private Integer positionX = 0;

    /** INT NOT NULL DEFAULT 0 */
    @Column(name = "position_y", nullable = false)
    private Integer positionY = 0;

    /** ENUM('ACTIVE','MAINTENANCE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RackStatus status = RackStatus.ACTIVE;

    /** BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "catalog_visible", nullable = false)
    private boolean catalogVisible = false;

    /** DECIMAL(10,2) NULL */
    @Column(name = "catalog_price", precision = 10, scale = 2)
    private BigDecimal catalogPrice;

    /** INT NOT NULL DEFAULT 0 */
    @Column(name = "catalog_stock", nullable = false)
    private Integer catalogStock = 0;

    /** VARCHAR(255) NULL */
    @Column(name = "catalog_summary", length = 255)
    private String catalogSummary;

    /** 1 rack -> N equipos */
    @OneToMany(mappedBy = "rack", fetch = FetchType.LAZY)
    private Set<Equipment> equipments = new HashSet<>();

    /**
     * 1 rack -> N permisos de acceso (user_rack_access)
     * Tabla puente con atributo extra (permission).
     */
    @OneToMany(mappedBy = "rack", fetch = FetchType.LAZY)
    private Set<UserRackAccess> userAccess = new HashSet<>();
}
