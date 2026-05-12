package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para `maintenance_work_orders`.
 *
 * Representa una orden de mantenimiento.
 * Relación:
 *  - Muchas órdenes pertenecen a 1 equipo (N:1).
 *  - Puede haber un usuario creador (created_by_user_id) (N:1 opcional).
 *  - 1 orden puede tener muchas notas (1:N).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"equipment", "createdBy", "notes"})
@Entity
@Table(name = "maintenance_work_orders")
public class MaintenanceWorkOrder {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> equipments.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /** FK NULL -> users.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /** ENUM('OPEN','IN_PROGRESS','CLOSED') NOT NULL DEFAULT 'OPEN' */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkOrderStatus status = WorkOrderStatus.OPEN;

    /** ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM' */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;

    /** VARCHAR(180) NOT NULL */
    @Column(name = "summary", nullable = false, length = 180)
    private String summary;

    /** TEXT NULL */
    @Lob
    @Column(name = "details")
    private String details;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP */
    @Column(name = "opened_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime openedAt;

    /** DATETIME NULL */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /** 1 orden -> N notas */
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private Set<MaintenanceNote> notes = new HashSet<>();
}
