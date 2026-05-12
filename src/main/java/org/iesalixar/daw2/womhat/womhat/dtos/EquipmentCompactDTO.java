package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;

/**
 * DTO compacto para mostrar equipos dentro de otras vistas.
 *
 * Ejemplo: detalle de rack, mapa o tarjetas resumen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCompactDTO {

    /** ID del equipo. */
    private Long id;

    /** Nombre del equipo. */
    private String name;

    /** Tipo del equipo. */
    private EquipmentType type;

    /** Número de serie. */
    private String serialNumber;

    /** Posición inicial en U dentro del rack. */
    private Integer slotPositionU;

    /** Altura en U ocupada por el equipo. */
    private Integer slotHeightU;

    /** Estado actual del equipo. */
    private EquipmentStatus status;
}