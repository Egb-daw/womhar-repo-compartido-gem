package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.DashboardRackMapItemDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DashboardSummaryDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper auxiliar para la pantalla de dashboard.
 *
 * Aunque parte de la información sale de varias consultas,
 * centralizar aquí el montaje de DTOs ayuda a mantener la capa
 * de servicio/controlador más limpia.
 */
public class DashboardMapper {

    /**
     * Construye el DTO resumen del dashboard a partir de valores ya calculados.
     */
    public static DashboardSummaryDTO toSummaryDTO(long totalRacks,
                                                   long totalEquipments,
                                                   long activeHosts,
                                                   long alerts) {
        return new DashboardSummaryDTO(totalRacks, totalEquipments, activeHosts, alerts);
    }

    /**
     * Convierte una colección de racks en los elementos del mapa del dashboard.
     */
    public static List<DashboardRackMapItemDTO> toRackMapItems(Collection<Rack> racks) {
        if (racks == null || racks.isEmpty()) {
            return List.of();
        }

        return racks.stream()
                .sorted(Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(RackMapper::toMapItemDTO)
                .toList();
    }
}