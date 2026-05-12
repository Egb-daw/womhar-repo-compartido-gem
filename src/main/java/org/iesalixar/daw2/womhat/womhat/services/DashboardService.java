package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.DashboardRackMapItemDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DashboardSummaryDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RecentActivityDTO;

import java.util.List;

/**
 * Servicio de negocio para el dashboard principal.
 */
public interface DashboardService {

    /**
     * Calcula el resumen superior del dashboard adaptado al usuario.
     *
     * @param email email del usuario autenticado
     * @return resumen filtrado por alcance
     */
    DashboardSummaryDTO getSummaryForUser(String email);

    /**
     * Devuelve los racks a mostrar en el mapa visual del dashboard
     * adaptados al alcance real del usuario.
     *
     * @param email email del usuario autenticado
     * @return racks visibles para ese usuario
     */
    List<DashboardRackMapItemDTO> getRackMapItemsForUser(String email);

    /**
     * Devuelve la actividad reciente visible para un usuario concreto.
     *
     * @param email email del usuario autenticado
     * @param limit máximo de filas
     * @return actividad reciente filtrada por alcance real
     */
    List<RecentActivityDTO> getRecentActivityForUser(String email, int limit);
}
