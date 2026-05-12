package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para poblar selects de salas.
 *
 * Muy útil en formularios de racks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomOptionDTO {

    /** ID de la sala. */
    private Long id;

    /** Nombre de la sala. */
    private String name;

    /** Planta o nivel de la sala. */
    private String floor;

    /** Código del CPD padre. */
    private String dataCenterCode;

    /** Nombre del CPD padre. */
    private String dataCenterName;
}