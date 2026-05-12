package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Equipment}.
 *
 * <p>Permite búsquedas por rack, número de serie, tipo y estado.</p>
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Busca un equipo por su número de serie.
     *
     * @param serialNumber número de serie
     * @return equipo si existe
     */
    Optional<Equipment> findBySerialNumber(String serialNumber);

    /**
     * Busca un equipo por su número de serie ignorando mayúsculas/minúsculas.
     *
     * @param serialNumber número de serie
     * @return equipo si existe
     */
    Optional<Equipment> findBySerialNumberIgnoreCase(String serialNumber);

    /**
     * Comprueba si ya existe un equipo con ese número de serie.
     *
     * @param serialNumber número de serie
     * @return true si existe
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     * Lista los equipos instalados en un rack.
     *
     * @param rackId id del rack
     * @return lista de equipos
     */
    List<Equipment> findByRack_Id(Long rackId);

    /**
     * Lista los equipos de un rack ordenados por nombre.
     *
     * @param rackId id del rack
     * @return lista ordenada
     */
    List<Equipment> findByRack_IdOrderByNameAsc(Long rackId);

    /**
     * Lista equipos por tipo.
     *
     * @param type tipo de equipo
     * @return lista filtrada
     */
    List<Equipment> findByType(EquipmentType type);

    /**
     * Lista equipos por estado.
     *
     * @param status estado del equipo
     * @return lista filtrada
     */
    List<Equipment> findByStatus(EquipmentStatus status);

    /**
     * Recupera un equipo por id cargando rack y especializaciones 1:1.
     *
     * @param id id del equipo
     * @return equipo detallado si existe
     */
    @EntityGraph(attributePaths = {"rack", "hostSpecification", "storageBackup", "networkElement"})
    Optional<Equipment> findDetailedById(Long id);
}