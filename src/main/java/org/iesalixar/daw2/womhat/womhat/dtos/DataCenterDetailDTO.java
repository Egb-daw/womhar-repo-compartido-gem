package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de detalle para CPD.
 *
 * Incluye las salas asociadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterDetailDTO {

    /** ID del CPD. */
    private Long id;

    /** Código del CPD. */
    private String code;

    /** Nombre del CPD. */
    private String name;

    /** Ciudad. */
    private String city;

    /** Edificio. */
    private String building;

    /** Estado del CPD. */
    private DataCenterStatus status;

    /** Salas registradas dentro del CPD. */
    private List<DataCenterRoomDTO> rooms = new ArrayList<>();
}