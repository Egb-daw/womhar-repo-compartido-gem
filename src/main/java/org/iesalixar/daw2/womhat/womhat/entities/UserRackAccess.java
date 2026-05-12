package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;

import java.time.LocalDateTime;

/**
 * Entidad JPA para `user_rack_access`.
 *
 * Esta tabla no es un simple N:M, porque tiene campos extra de permiso y trazabilidad.
 * Por eso se modela como entidad propia con PK compuesta (user_id, rack_id).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "rack"})
@Entity
@Table(name = "user_rack_access")
public class UserRackAccess {

    /** PK compuesta */
    @EmbeddedId
    private UserRackAccessId id = new UserRackAccessId();

    /**
     * Relación a User usando parte de la PK.
     * @MapsId("userId") sincroniza id.userId con user.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Relación a Rack usando parte de la PK.
     * @MapsId("rackId") sincroniza id.rackId con rack.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("rackId")
    @JoinColumn(name = "rack_id", nullable = false)
    private Rack rack;

    /** ENUM('READ','WRITE','ADMIN') NOT NULL DEFAULT 'READ' */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 10)
    private RackPermission permission = RackPermission.READ;

    /** Indica si este acceso representa al propietario funcional original del rack. */
    @Column(name = "original_owner", nullable = false)
    private boolean originalOwner = false;

    /** Usuario que concedió o actualizó este acceso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_user_id")
    private User grantedBy;

    /** Fecha/hora en la que se concedió o actualizó el acceso. */
    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();
}
