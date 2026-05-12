package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato de servicio para gestión de racks.
 *
 * <p>Centraliza la lógica de inventario, visibilidad por permisos y
 * transformación entre entidades y DTOs de rack.</p>
 */
public interface RackService {

    /**
     * Lista racks con alcance global (uso administrativo).
     */
    Page<RackDTO> list(Pageable pageable);

    /**
     * Lista racks visibles para el usuario autenticado según permisos efectivos.
     */
    Page<RackDTO> list(Pageable pageable, String currentUserEmail);

    /**
     * Obtiene racks vinculados a una sala concreta.
     */
    List<RackDTO> listByRoom(Long roomId);

    /**
     * Devuelve opciones ligeras de rack para selectores de formulario.
     */
    List<RackOptionDTO> listOptions();

    /**
     * Carga detalle de rack sin filtrar por usuario.
     */
    RackDetailDTO getDetail(Long id);

    /**
     * Carga detalle de rack validando visibilidad para el usuario actual.
     */
    RackDetailDTO getDetail(Long id, String currentUserEmail);

    /**
     * Obtiene DTO de formulario para edición.
     */
    RackFormDTO getForm(Long id);

    /**
     * Crea un rack nuevo.
     */
    void create(RackFormDTO dto);

    /**
     * Actualiza un rack existente.
     */
    void update(RackFormDTO dto);

    /**
     * Elimina un rack existente.
     */
    void delete(Long id);
}
