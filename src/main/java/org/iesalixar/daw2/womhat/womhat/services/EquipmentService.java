package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentEventLogDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato de servicio para gestión del inventario de equipos.
 *
 * <p>Incluye lectura filtrada por permisos, mantenimiento de estado de ciclo
 * de vida y trazabilidad de eventos sobre cada equipo.</p>
 */
public interface EquipmentService {

    /**
     * Lista equipos con alcance global.
     */
    Page<EquipmentDTO> list(Pageable pageable);

    /**
     * Lista equipos permitiendo incluir estados retirados/archivados.
     */
    Page<EquipmentDTO> list(Pageable pageable, boolean includeClosed);

    /**
     * Lista equipos visibles para un usuario concreto.
     */
    Page<EquipmentDTO> list(Pageable pageable, String currentUserEmail);

    /**
     * Lista equipos visibles para usuario permitiendo incluir estados cerrados.
     */
    Page<EquipmentDTO> list(Pageable pageable, String currentUserEmail, boolean includeClosed);

    /**
     * Carga el detalle completo de un equipo.
     */
    EquipmentDetailDTO getDetail(Long id);

    /**
     * Carga detalle de equipo validando alcance del usuario autenticado.
     */
    EquipmentDetailDTO getDetail(Long id, String currentUserEmail);

    /**
     * Obtiene DTO de formulario para crear/editar equipo.
     */
    EquipmentFormDTO getForm(Long id);

    /**
     * Devuelve el histórico técnico de eventos de un equipo.
     */
    List<EquipmentEventLogDTO> getEventLog(Long equipmentId);

    /**
     * Crea un nuevo equipo bajo el actor autenticado.
     */
    void create(EquipmentFormDTO dto, String actorEmail);

    /**
     * Actualiza un equipo existente bajo el actor autenticado.
     */
    void update(EquipmentFormDTO dto, String actorEmail);

    /**
     * Cambia el estado operativo de un equipo y registra trazabilidad.
     */
    void changeStatus(Long id, EquipmentStatus status, String actorEmail);

    /**
     * Elimina físicamente un equipo (uso restringido).
     */
    void delete(Long id, String actorEmail);

    /**
     * Lista equipos pertenecientes a un rack concreto.
     */
    List<EquipmentDTO> listByRack(Long rackId);
}
