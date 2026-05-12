package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para `maintenance_notes`.
 *
 * Nota asociada a una orden de mantenimiento.
 * Relación:
 *  - Muchas notas pertenecen a 1 orden (N:1).
 *  - Puede haber un usuario creador (N:1 opcional).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"workOrder", "createdBy"})
@Entity
@Table(name = "maintenance_notes")
public class MaintenanceNote {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> maintenance_work_orders.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private MaintenanceWorkOrder workOrder;

    /** FK NULL -> users.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /** TEXT NOT NULL */
    @Lob
    @Column(name = "note", nullable = false)
    private String note;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
