package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;

import java.time.LocalDateTime;

/**
 * DTO de lectura para la entidad UserRackAccess.
 *
 * Sirve para mostrar qué racks puede leer o gestionar un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRackAccessDTO {

    /** ID del usuario. */
    private Long userId;

    /** Email del usuario. */
    private String userEmail;

    /** ID del rack. */
    private Long rackId;

    /** Etiqueta visible del rack. */
    private String rackLocationLabel;

    /** Permiso concreto sobre ese rack. */
    private RackPermission permission;

    /** Indica si este usuario es el propietario funcional original del rack. */
    private boolean originalOwner;

    /** Email del usuario que concedió o actualizó el acceso. */
    private String grantedByEmail;

    /** Fecha/hora de concesión o última actualización del acceso. */
    private LocalDateTime grantedAt;
}
