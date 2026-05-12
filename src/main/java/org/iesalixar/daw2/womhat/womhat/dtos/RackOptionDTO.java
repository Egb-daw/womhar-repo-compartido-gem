package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;

/**
 * DTO simple para poblar selects de racks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackOptionDTO {

    /** ID del rack. */
    private Long id;

    /** Etiqueta visible del rack. */
    private String locationLabel;

    /** Nombre de la sala donde se encuentra. */
    private String roomName;

    /** Capacidad del rack en U. */
    private Integer capacityU;

    /** Función principal del rack. */
    private String functionName;

    /** Estado del rack. */
    private RackStatus status;
}