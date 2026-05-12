package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteFormDTO;

import java.util.List;

/**
 * Servicio de negocio para notas de mantenimiento.
 */
public interface MaintenanceNoteService {

    /**
     * Lista notas de una orden verificando permisos de lectura por rack.
     */
    List<MaintenanceNoteDTO> listByWorkOrder(Long workOrderId, String actorEmail);

    /**
     * Inserta una nota técnica asociada a una orden de mantenimiento.
     */
    void create(MaintenanceNoteFormDTO dto, String actorEmail);

    /**
     * Elimina una nota (acción reservada a administración global).
     */
    void delete(Long id, String actorEmail);
}
