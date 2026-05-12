package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumen para las tarjetas superiores del dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    /** Número total de racks registrados. */
    private Long totalRacks;

    /** Número total de equipos registrados. */
    private Long totalEquipments;

    /** Número de hosts activos. */
    private Long activeHosts;

    /** Número de alertas o incidencias. */
    private Long alerts;
}