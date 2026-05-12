package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;

import java.util.List;

public interface MapcpdService {

    /**
     * Obtiene todos los racks con sus equipos para el mapa.
     * Retorna una lista de RackDTO que incluye la información de los equipos.
     */
    List<RackDTO> getMapData();

    /**
     * Guarda o actualiza un rack completo con sus equipos.
     */
    RackDTO saveRackWithEquipment(RackDTO rackDTO);

    /**
     * Elimina un rack por ID.
     */
    void deleteRack(Long id);
}