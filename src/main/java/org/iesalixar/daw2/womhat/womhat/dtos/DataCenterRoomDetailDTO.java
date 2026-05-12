package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de detalle para salas del CPD.
 *
 * Incluye la lista de racks asociados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterRoomDetailDTO {

    /** ID de la sala. */
    private Long id;

    /** ID del CPD al que pertenece. */
    private Long dataCenterId;

    /** Código del CPD padre. */
    private String dataCenterCode;

    /** Nombre del CPD padre. */
    private String dataCenterName;

    /** Nombre de la sala. */
    private String name;

    /** Planta o nivel. */
    private String floor;

    /** Notas sobre la sala. */
    private String notes;

    /** Racks contenidos en esta sala. */
    private List<RackDTO> racks = new ArrayList<>();
}