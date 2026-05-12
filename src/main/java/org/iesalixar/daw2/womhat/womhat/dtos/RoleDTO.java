package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de lectura para la entidad Role.
 *
 * Se usa en listados, selects y detalles de usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    /** ID interno del rol. */
    private Long id;

    /** Nombre técnico del rol. */
    private String name;

    /** Nombre visible para interfaz. */
    private String displayName;

    /** Descripción opcional del rol. */
    private String description;
}