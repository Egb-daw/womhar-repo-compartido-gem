package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.NetworkConnectionType;

/**
 * DTO de lectura para network_elements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkElementDTO {

    /** ID del equipo asociado. */
    private Long equipmentId;

    /** Tipo de conexión del elemento de red. */
    private NetworkConnectionType connection;

    /** Número total de puertos. */
    private Integer totalPorts;
}